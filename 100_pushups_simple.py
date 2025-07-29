# -*- coding: utf-8 -*-
"""
Created on Fri Jan 24 13:56:06 2025

@author: brend
"""

"""
100 Pushups Tracker with Enhanced Features
Based on plan from https://hundredpushups.com/

FUTURE EXPANSION IDEAS:
- Adaptive Plan Progression for automatically adjusting Week/Day/Column based on performance.
- Enhanced User Interface (graphical or web-based) for improved usability.
"""

import csv
import os
import time
import random
from datetime import datetime

try:
    import matplotlib.pyplot as plt
except ImportError:
    plt = None  # Matplotlib may not be available; this variable remains None if so

# -- GLOBAL / CUSTOMIZABLE SETTINGS --

PLAN_CSV = "100_pushups_plan.csv"                # Path to the plan CSV
ATTEMPT_LOG_CSV = "100_pushups_attempt_log.csv"  # Path to the attempt log CSV

# Determines the default answer for starting the rest timer.
# Acceptable values: 'y' (yes) or 'n' (no).
USE_TIMER_DEFAULT = 'y'

# If True, creates a backup of ATTEMPT_LOG_CSV each time an attempt is logged or edited.
ENABLE_BACKUP = True

# If > 0, restricts the number of backup files to this many. Excess backups are deleted.
# Example: If 5 backups already exist, creating a 6th backup removes the oldest one.
MAX_BACKUPS = 5

# If True, partial success is tracked. If a user does fewer reps than recommended for some sets
# (but at least 1), the outcome can be "PARTIAL." Otherwise, any shortfall is treated as "INCOMPLETE."
PARTIAL_SUCCESS_ENABLED = True


def backup_log_file():
    """
    Creates a timestamped backup copy of the ATTEMPT_LOG_CSV, if ENABLE_BACKUP is True.
    After creating the backup, calls prune_backups() if MAX_BACKUPS > 0.
    """
    if not ENABLE_BACKUP:
        return
    if not os.path.isfile(ATTEMPT_LOG_CSV):
        return

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    backup_filename = f"{ATTEMPT_LOG_CSV}.{timestamp}.bak"
    try:
        with open(ATTEMPT_LOG_CSV, "rb") as src, open(backup_filename, "wb") as dst:
            dst.write(src.read())
        print(f"Backup created: {backup_filename}")
    except Exception as e:
        print(f"Backup failed: {e}")
        return

    if MAX_BACKUPS > 0:
        prune_backups()


def prune_backups():
    """
    Removes older backups if the total number of backups exceeds MAX_BACKUPS.
    Gathers all files matching ATTEMPT_LOG_CSV.*.bak, sorts them by creation time,
    and keeps only the most recent MAX_BACKUPS.
    """
    # Example backup filenames might look like:
    #   "100_pushups_attempt_log.csv.20250124_120000.bak"
    #   "100_pushups_attempt_log.csv.20250124_120100.bak"
    # This function finds them all, sorts by creation time, and removes the oldest ones.
    pattern_prefix = f"{ATTEMPT_LOG_CSV}."
    pattern_suffix = ".bak"

    # Gather all backups that start with "ATTEMPT_LOG_CSV." and end with ".bak"
    # Use a simple approach: check if a file matches these prefix/suffix conditions.
    all_files = os.listdir(".")
    backup_files = []
    for f in all_files:
        if f.startswith(pattern_prefix) and f.endswith(pattern_suffix):
            # Attempt to parse out the middle part for time sorting
            backup_files.append(f)

    if len(backup_files) <= MAX_BACKUPS:
        return  # No pruning needed

    # Sort by creation or modification time (oldest first).
    # On Windows, os.path.getmtime will be used. On other systems, same approach.
    backup_files.sort(key=lambda x: os.path.getmtime(x))

    # Remove old ones, keep only the newest MAX_BACKUPS
    excess_count = len(backup_files) - MAX_BACKUPS
    to_remove = backup_files[:excess_count]
    for old_backup in to_remove:
        try:
            os.remove(old_backup)
            print(f"Removed old backup: {old_backup}")
        except Exception as e:
            print(f"Failed to remove {old_backup}: {e}")


def load_plan(csv_filename):
    """
    Loads a pushups plan from the specified CSV file into a list of dictionaries.
    Each row can have up to 9 set columns: Set1..Set8 plus SetFinal.
    Skips empty set columns gracefully.
    """
    plan_data = []
    if not os.path.exists(csv_filename):
        print(f"ERROR: Plan file '{csv_filename}' not found.")
        return plan_data

    with open(csv_filename, 'r', newline='', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            try:
                w = int(row['Week'])
                d = int(row['Day'])
                c = row['Column'].strip()

                # Collect all set columns in a list (some may be blank)
                possible_set_cols = ['Set1', 'Set2', 'Set3', 'Set4', 'Set5', 'Set6', 'Set7', 'Set8', 'SetFinal']
                sets_list = []
                for col_name in possible_set_cols:
                    val = row.get(col_name, "").strip()
                    if val:  # only add if non-empty
                        sets_list.append(val)

                rest = row['RecommendedRest'].strip()

                plan_data.append({
                    'week': w,
                    'day': d,
                    'column': c,
                    'sets': sets_list,       # now can be 1..9 sets
                    'rest': rest
                })
            except (ValueError, KeyError):
                continue
    return plan_data


def find_plan_entry(plan_data, week, day, column):
    """
    Searches for an entry in the loaded plan matching (week, day, column).
    Returns the matching dictionary or None if not found.
    """
    for entry in plan_data:
        if (entry['week'] == week and
            entry['day'] == day and
            entry['column'] == column):
            return entry
    return None


def parse_set_minimum(set_str):
    """
    Interprets a plan set string. For example:
      "10" => 10
      "MAX≥12" => 12
    If parsing fails, 0 is returned.
    """
    s = set_str.upper().strip()
    if s.startswith("MAX≥"):
        parts = s.split("≥")
        if len(parts) == 2:
            try:
                return int(parts[1])
            except ValueError:
                return 0
    else:
        try:
            return int(s)
        except ValueError:
            return 0
    return 0


def rest_timer(rest_str):
    """
    Displays a rest timer that attempts to handle possible ranges or plus notation:
      - For "60-90s", picks a random duration between 60 and 90 seconds.
      - For "90s+", defaults to 90 seconds.
      - If the user presses Ctrl+C, the timer is skipped.
    """
    import re
    duration = 60
    # Example strings:
    #   "60-90s" => random int between 60 and 90
    #   "90s+" => 90
    #   "60-90s+" => possible extension, but not standard. Could parse similarly.

    range_match = re.match(r"(\d+)-(\d+)s", rest_str)
    if range_match:
        low_sec = int(range_match.group(1))
        high_sec = int(range_match.group(2))
        if low_sec < high_sec:
            duration = random.randint(low_sec, high_sec)
    elif rest_str.endswith("+"):
        plus_match = re.search(r"(\d+)", rest_str)
        if plus_match:
            duration = int(plus_match.group(1))
    else:
        match = re.search(r"(\d+)", rest_str)
        if match:
            duration = int(match.group(1))

    print(f"\nRest for {duration} second(s) (press Ctrl+C to skip).")

    try:
        end_time = time.time() + duration
        while True:
            remaining = int(end_time - time.time())
            if remaining <= 0:
                break
            print(f"\r{remaining} ", end="", flush=True)
            time.sleep(1)
        print("\nRest complete!\n")
    except KeyboardInterrupt:
        print("\nRest timer skipped!\n")


def do_session(plan_entry):
    """
    Guides a user through each set of a session. Returns a list of tuples indicating
    (actual_reps, recommended_reps) for each set, along with a boolean indicating
    whether the session was quit or completed. The recommended_reps value is derived
    from parse_set_minimum() for comparison after the session.

    Example return:
      ([(10,10), (8,8), (5,5), (12,12), (15,15)], True)  # session fully completed
      ([(10,10), (8,8)], False)  # user quit early
    """
    sets_planned = plan_entry['sets']
    rest_str = plan_entry['rest']
    set_data = []
    total_sets = len(sets_planned)

    w, d, c = plan_entry['week'], plan_entry['day'], plan_entry['column']
    print(f"\n--- Session: Week {w}, Day {d}, Col {c} ---")
    print(f"Rest: {rest_str}")
    print("Planned sets:", ", ".join(sets_planned), "\n")

    for i, set_str in enumerate(sets_planned, start=1):
        recommended = parse_set_minimum(set_str)
        final_set_with_max = (i == total_sets and "MAX≥" in set_str.upper())

        if final_set_with_max:
            # Prompt for how many were actually done in the final "MAX≥X" set
            print(f"Final set: {set_str} pushups.")
            while True:
                try:
                    actual = int(input("How many pushups were completed in the final set? "))
                    if actual < 0:
                        raise ValueError
                    set_data.append((actual, recommended))
                    break
                except ValueError:
                    print("Invalid input. Please enter a non-negative integer.")
        else:
            print(f"Set {i}: {set_str} pushups.")
            action = input("Press Enter to confirm completion or type 'quit' to stop: ").strip().lower()
            if action == 'quit':
                print("Session was stopped before completion.")
                return set_data, False
            # Optionally, prompt for actual reps instead of assuming
            # For simplicity, we'll assume completion of the planned reps
            set_data.append((recommended, recommended))

        if i < total_sets:
            # Show the next set's pushup count
            next_set_str = sets_planned[i]
            print(f"Next set will be: {next_set_str} pushups.\n")

            # Ask whether to start the rest timer
            use_timer = input(f"Start rest timer? (default={USE_TIMER_DEFAULT}): ").strip().lower()
            if not use_timer:
                use_timer = USE_TIMER_DEFAULT
            if use_timer in ["y", "yes"]:
                rest_timer(rest_str)

    return set_data, True


def determine_outcome(set_data, session_completed):
    """
    Determines the session outcome based on partial success rules:
      - If session_completed is False, return "INCOMPLETE".
      - If session_completed is True, compare actual_reps to recommended_reps in set_data.
        If every actual >= recommended, return "SUCCESS".
        If at least one actual is > 0 but < recommended, return "PARTIAL" (if PARTIAL_SUCCESS_ENABLED).
        Otherwise, return "INCOMPLETE".
    """
    if not session_completed:
        return "INCOMPLETE"
    if not set_data:
        return "INCOMPLETE"

    all_full = True
    partial_found = False
    for (actual, recommended) in set_data:
        if actual < recommended:
            all_full = False
            if actual > 0:
                partial_found = True

    if all_full:
        return "SUCCESS"
    else:
        if PARTIAL_SUCCESS_ENABLED and partial_found:
            return "PARTIAL"
        else:
            return "INCOMPLETE"


def log_attempt(week, day, column, set_data, outcome):
    """
    Logs an attempt to the CSV file. If ENABLE_BACKUP is True, creates a backup first.
    The set_data parameter is a list of (actual, recommended) for each set.
    The outcome parameter is a string: "SUCCESS", "PARTIAL", or "INCOMPLETE".
    """
    backup_log_file()  # Only creates a backup if ENABLE_BACKUP is True

    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    # Store only the actual reps in the CSV
    actuals_only = [str(tup[0]) for tup in set_data]
    sets_str = "|".join(actuals_only)

    file_exists = os.path.isfile(ATTEMPT_LOG_CSV)
    with open(ATTEMPT_LOG_CSV, "a", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        if not file_exists:
            writer.writerow(["timestamp", "week", "day", "column", "outcome", "sets_completed"])
        writer.writerow([timestamp, week, day, column, outcome, sets_str])

    print(f"\nLogged attempt: {outcome} => {sets_str}")


def log_test_attempt(num_pushups):
    """
    Logs a single-set max test attempt. This uses week=-1, day=-1, column="TEST", outcome="TEST".
    Backup is created first if ENABLE_BACKUP is True.
    """
    backup_log_file()  # Will only back up if ENABLE_BACKUP

    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    sets_str = str(num_pushups)
    outcome = "TEST"

    file_exists = os.path.isfile(ATTEMPT_LOG_CSV)
    with open(ATTEMPT_LOG_CSV, "a", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        if not file_exists:
            writer.writerow(["timestamp", "week", "day", "column", "outcome", "sets_completed"])
        writer.writerow([timestamp, -1, -1, "TEST", outcome, sets_str])

    print(f"\nLogged TEST attempt: single-set max = {num_pushups}")


def get_attempts():
    """
    Retrieves and parses all attempts from the attempt log, sorted by timestamp.
    Returns a list of dictionaries, each containing:
      timestamp, week, day, column, outcome, sets_completed (as ints).
    """
    if not os.path.isfile(ATTEMPT_LOG_CSV):
        return []
    with open(ATTEMPT_LOG_CSV, "r", newline="", encoding="utf-8") as f:
        rows = list(csv.DictReader(f))
        attempts = []
        for row in rows:
            sets_list = [int(x) for x in row["sets_completed"].split("|") if x.isdigit()]
            attempts.append({
                "timestamp": row["timestamp"],
                "week": int(row["week"]),
                "day": int(row["day"]),
                "column": row["column"],
                "outcome": row["outcome"],
                "sets_completed": sets_list
            })
        attempts.sort(key=lambda a: a["timestamp"])
        return attempts


def get_last_attempt():
    """
    Returns the most recent attempt (including TEST attempts), or None if no attempts exist.
    """
    all_ = get_attempts()
    if not all_:
        return None
    return all_[-1]


def get_last_normal_attempt():
    """
    Returns the most recent attempt whose outcome is not "TEST," or None if no such attempt exists.
    """
    all_ = get_attempts()
    for attempt in reversed(all_):
        if attempt["outcome"] != "TEST":
            return attempt
    return None


def show_progress_chart_by_date(suggest_test=False):
    """
    Reads the attempt log and creates a bar chart showing total pushups completed
    (or single-set count for TEST attempts) in chronological order.
    Color code:
      - SUCCESS: green
      - PARTIAL: orange
      - INCOMPLETE: red
      - TEST: blue
    The x-axis is the date (YYYY-MM-DD), and the y-axis is the number of pushups.
    Adds little black lines on each bar indicating the cumulative pushups per set.
    The black lines now fully match the width of the relevant bars.
    If suggest_test=True, a note is added to the chart title.
    """
    if not plt:
        print("matplotlib not available. Chart cannot be displayed.\n")
        return

    attempts = get_attempts()
    if not attempts:
        print("No attempts found in the log.\n")
        return

    x_vals = range(len(attempts))
    y_vals = []
    colors = []
    x_labels = []

    for a in attempts:
        dt_obj = datetime.strptime(a["timestamp"], "%Y-%m-%d %H:%M:%S")
        date_label = dt_obj.strftime("%Y-%m-%d")
        total_pushups = sum(a["sets_completed"])

        outcome = a["outcome"]
        if outcome == "SUCCESS":
            colors.append("green")
        elif outcome == "PARTIAL":
            colors.append("orange")
        elif outcome == "INCOMPLETE":
            colors.append("red")
        elif outcome == "TEST":
            colors.append("blue")
        else:
            colors.append("gray")

        y_vals.append(total_pushups)
        x_labels.append(date_label)

    plt.figure(figsize=(10, 6))
    bars = plt.bar(x_vals, y_vals, color=colors, edgecolor='black')  # Added edgecolor for better visibility

    plt.xticks(x_vals, x_labels, rotation=45, ha="right")
    plt.xlabel("Date of Attempt")
    plt.ylabel("Total Pushups (Session or Test)")
    title_text = "100 Pushups Progress (by Date)"
    if suggest_test:
        title_text += "\n(A test is suggested based on recent progression.)"
    plt.title(title_text, fontsize=14)

    # Access the current Axes instance
    ax = plt.gca()

    # Iterate over each bar and add cumulative set lines
    for bar, attempt in zip(bars, attempts):
        sets = attempt["sets_completed"]
        cumulative = 0
        for set_count in sets:
            cumulative += set_count
            # Calculate the left and right edges of the bar
            bar_left = bar.get_x()
            bar_right = bar.get_x() + bar.get_width()
            # Draw a horizontal line spanning the full width of the bar at the cumulative height
            ax.hlines(cumulative,
                      bar_left,
                      bar_right,
                      colors='black',
                      linestyles='-',
                      linewidth=1)

    plt.tight_layout()
    plt.show()

def print_last_attempt_info():
    """
    Displays details of the most recent attempt (including TEST attempts).
    Also checks if the last normal attempt was Week 2 Day 3 or Week 4 Day 3 with SUCCESS,
    which suggests performing a TEST attempt.
    Additionally, displays the total pushups completed in the last attempt.
    """
    last_any = get_last_attempt()
    if not last_any:
        print("No attempts have been logged yet.\n")
        return

    print("=== Last Attempt ===")
    print(f"Date/Time: {last_any['timestamp']}")
    outcome = last_any["outcome"]
    if outcome == "TEST":
        max_pushups = sum(last_any["sets_completed"])
        print(f"Recent attempt was a TEST: single-set max = {max_pushups}\n")
    else:
        w, d, c = last_any["week"], last_any["day"], last_any["column"]
        sets_done = last_any["sets_completed"]
        total_pushups = sum(sets_done)

        print(f"Week: {w}, Day: {d}, Column: {c}")
        print(f"Outcome: {outcome}")
        print(f"Sets Completed: {sets_done}")
        print(f"Total Pushups: {total_pushups}\n")

    last_normal = get_last_normal_attempt()
    if last_normal and last_normal["outcome"] == "SUCCESS":
        if (last_normal["week"] == 2 and last_normal["day"] == 3) \
           or (last_normal["week"] == 4 and last_normal["day"] == 3):
            print("NOTE: The 100 Pushups Challenge recommends a TEST attempt "
                  f"after completing Week {last_normal['week']}, Day {last_normal['day']} successfully.\n")


def next_session_from_last_normal(last_normal):
    """
    Calculates the next session based on the last normal attempt.
    If Day < 3, increment Day by 1; otherwise, increment Week and reset Day to 1.
    The column remains unchanged.
    """
    w = last_normal["week"]
    d = last_normal["day"]
    c = last_normal["column"]
    if d < 3:
        return (w, d + 1, c)
    else:
        return (w + 1, 1, c)


def show_next_session_preview(week, day, column, plan_data):
    """
    Looks up the next session in the plan data (week, day, column)
    and displays the sets if found.
    """
    entry = find_plan_entry(plan_data, week, day, column)
    if entry:
        sets_str = ", ".join(entry['sets'])
        print(f"Upcoming Session: Week {week}, Day {day}, Column {column}")
        print(f"Planned sets: {sets_str}")
        print(f"Rest: {entry['rest']}\n")
    else:
        print(f"No plan entry found for Week {week}, Day {day}, Column {column}.\n")


def edit_log():
    """
    Allows viewing, editing, or removing log entries. This function can be considered
    part of 'production hardening.' It loads all attempts, displays them with an index,
    and allows the user to remove or correct an entry. The code then rewrites the CSV file
    with the changes. If ENABLE_BACKUP is True, a backup is created before modifying the file.
    """
    attempts = get_attempts()
    if not attempts:
        print("No attempts in the log. Nothing to edit.")
        return

    print("\n--- Edit Attempt Log ---")
    for idx, a in enumerate(attempts):
        print(f"{idx}: {a['timestamp']} | W{a['week']}D{a['day']} Col={a['column']} "
              f"Outcome={a['outcome']} Sets={a['sets_completed']}")

    selection = input("\nEnter the index of the attempt to edit/remove (or 'cancel' to exit): ").strip().lower()
    if selection == "cancel":
        print("Edit canceled.")
        return
    try:
        choice_idx = int(selection)
        if choice_idx < 0 or choice_idx >= len(attempts):
            print("Invalid index.")
            return
    except ValueError:
        print("Invalid selection.")
        return

    chosen_attempt = attempts[choice_idx]
    print(f"Selected Attempt:\n{chosen_attempt}")

    print("\nWhat would you like to do with this attempt?")
    print("1) Remove the attempt (r)")
    print("2) Modify the outcome (m)")
    print("3) Cancel and do nothing (c)")

    action = input("Enter your choice: ").strip().lower()
    if action in ['1', 'r']:
        backup_log_file()  # Backup before removal
        del attempts[choice_idx]
        print("Attempt removed.")
    elif action in ['2', 'm']:
        new_outcome = input("Enter new outcome (SUCCESS, PARTIAL, INCOMPLETE, TEST): ").strip().upper()
        if new_outcome not in ("SUCCESS", "PARTIAL", "INCOMPLETE", "TEST"):
            print("Invalid outcome.")
            return
        backup_log_file()  # Backup before modifying
        chosen_attempt["outcome"] = new_outcome
        print(f"Outcome changed to {new_outcome}.")
    elif action in ['3', 'c']:
        print("No changes made.")
        return
    else:
        print("No valid action specified. No changes made.")
        return

    try:
        with open(ATTEMPT_LOG_CSV, "w", newline="", encoding="utf-8") as f:
            writer = csv.writer(f)
            writer.writerow(["timestamp", "week", "day", "column", "outcome", "sets_completed"])
            for att in attempts:
                sets_str = "|".join(map(str, att["sets_completed"]))
                writer.writerow([
                    att["timestamp"],
                    att["week"],
                    att["day"],
                    att["column"],
                    att["outcome"],
                    sets_str
                ])
        print("Log updated successfully.")
    except Exception as e:
        print(f"Error while updating log: {e}")


def do_test():
    """
    Prompts the user for a single-set maximum pushups value. The result is then
    logged as an outcome="TEST" attempt.
    """
    while True:
        try:
            val = int(input("Enter the maximum number of pushups in a single unbroken set: "))
            if val < 0:
                raise ValueError
            break
        except ValueError:
            print("Enter a non-negative integer.")
    log_test_attempt(val)


def main():
    """
    Main function that repeatedly displays a menu:
      1) Attempt the logical next session from the last NON-TEST attempt
      2) Repeat the last NON-TEST attempt
      3) Specify a session (Week/Day/Column)
      4) Perform a TEST
      5) Edit/Remove Attempts in the log
      6) Exit

    Shows the progress chart and last attempt info before each menu display.
    Also previews the planned sets for the next session.
    """
    plan_data = load_plan(PLAN_CSV)

    while True:
        # Check if a test suggestion is appropriate for the chart title
        suggest_test_flag = False
        ln = get_last_normal_attempt()
        if ln and ln["outcome"] == "SUCCESS":
            if (ln["week"] == 2 and ln["day"] == 3) or (ln["week"] == 4 and ln["day"] == 3):
                suggest_test_flag = True

        print("\nDisplaying the current progress chart (if any data is available)...")
        show_progress_chart_by_date(suggest_test=suggest_test_flag)

        print_last_attempt_info()

        # Determine the next session to preview
        if ln:
            w_next, d_next, c_next = next_session_from_last_normal(ln)
            show_next_session_preview(w_next, d_next, c_next, plan_data)

        print("MENU:")
        print("1) Attempt the next session (based on last NON-TEST attempt)")
        print("2) Repeat the last NON-TEST attempt")
        print("3) Specify a session (Week/Day/Column)")
        print("4) Perform a TEST (single-set max pushups)")
        print("5) Edit/Remove attempts in the log")
        print("6) Exit")

        choice = input("Enter menu choice: ").strip()

        if choice == "1":
            ln = get_last_normal_attempt()
            if not ln:
                print("No normal (non-TEST) attempt found. Option 3 can specify an initial session.")
                continue
            w, d, c = next_session_from_last_normal(ln)
            plan_entry = find_plan_entry(plan_data, w, d, c)
            if not plan_entry:
                print(f"No plan entry found for Week {w}, Day {d}, Column {c}.")
                print("Try specifying a session manually.")
                continue

            # Preview upcoming session
            show_next_session_preview(w, d, c, plan_data)

            set_data, completed_flag = do_session(plan_entry)
            outcome = determine_outcome(set_data, completed_flag)
            log_attempt(w, d, c, set_data, outcome)

        elif choice == "2":
            ln = get_last_normal_attempt()
            if not ln:
                print("No normal (non-TEST) attempt to repeat.")
                continue
            w, d, c = ln["week"], ln["day"], ln["column"]
            plan_entry = find_plan_entry(plan_data, w, d, c)
            if not plan_entry:
                print(f"No plan entry found for Week {w}, Day {d}, Column {c}.")
                continue

            # Preview repeating session
            print("\nRepeating the last session:")
            show_next_session_preview(w, d, c, plan_data)

            set_data, completed_flag = do_session(plan_entry)
            outcome = determine_outcome(set_data, completed_flag)
            log_attempt(w, d, c, set_data, outcome)

        elif choice == "3":
            w = get_int_in_range("Enter Week (1-6): ", 1, 6)
            d = get_int_in_range("Enter Day (1-3): ", 1, 3)
            col = ""
            while col not in ["1", "2", "3"]:
                col = input("Enter Column (1, 2, or 3): ").strip()
            plan_entry = find_plan_entry(plan_data, w, d, col)
            if not plan_entry:
                print("No matching session was found in the plan.\n")
                continue

            # Preview specified session
            show_next_session_preview(w, d, col, plan_data)

            set_data, completed_flag = do_session(plan_entry)
            outcome = determine_outcome(set_data, completed_flag)
            log_attempt(w, d, col, set_data, outcome)

        elif choice == "4":
            do_test()

        elif choice == "5":
            edit_log()

        elif choice == "6":
            print("Exiting the 100 Pushups tracker.")
            break

        else:
            print("Invalid choice. Please try again.")


def get_int_in_range(prompt, min_val, max_val):
    """
    Prompts for an integer within [min_val, max_val]. Retries until valid input is provided.
    """
    while True:
        try:
            val = int(input(prompt))
            if val < min_val or val > max_val:
                raise ValueError
            return val
        except ValueError:
            print(f"Please enter an integer between {min_val} and {max_val}.")


if __name__ == "__main__":
    main()
