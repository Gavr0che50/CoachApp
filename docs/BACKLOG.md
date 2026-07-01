# Backlog

## Daily activity histogram and next-morning insight

Goal: add a phone dashboard that aggregates daily activity and CoachApp workouts into one bar per day, then sends a next-morning notification summarizing yesterday versus the recent baseline.

Data sources:

- Health Connect walking data, including steps and walking calories when available.
- Health Connect active time and active energy expenditure when available.
- CoachApp workout sessions, including estimated calories, completed sets, volume, and duration.

UI target:

- Add a histogram-style chart where each bar represents one day.
- A bar should show total estimated calories burned from activity sources available that day.
- Distinguish Health Connect activity and CoachApp workout contribution visually.
- Keep missing data explicit, not silently counted as zero when permission or source data is unavailable.

Notification target:

- Send a morning notification based on the previous completed day.
- Compare yesterday to the recent rolling average, for example the previous 7 comparable days with available data.
- Classify the day as:
  - `plutot actif`
  - `plutot non actif`
  - `plutot constant`
- Mention that the result is an estimate when calories are included.

Acceptance notes:

- The comparison threshold must be documented and testable.
- The notification must not be guilt-driven.
- Health Connect and CoachApp-derived calories must remain source-visible.
- If Health Connect permissions are missing, the feature should still show CoachApp-only data with a clear missing-source note.

## Session flow and exercise focus

Goal: support real gym constraints during a workout session.

Done in UI:
- The phone session can switch the focused exercise card when a machine is occupied.
- Each exercise card tracks its own completed set count.
- Completed exercise cards are greyed out and show a green check overlay after the last set is validated, including when validation comes from Wear OS actions.

Next backend step:
- Persist performed sets at exercise/set granularity, not only the session aggregate. Store workout day id, exercise id/name, set index, planned reps, performed reps when available, planned/effective weight, rest duration, timestamps, and source (`phone` or `wear`).
- Use those performed-set rows as the source for workout history, daily activity histogram CoachApp contribution, and future progression suggestions.
- Add a repository query that returns daily CoachApp calories, duration, completed sets, and volume grouped by local date.

Acceptance notes:
- Switching focus must never duplicate completed sets.
- Reopening a partially completed exercise must resume on its next incomplete set.
- Session completion must still save exactly once.

Backend status:
- Done: performed sets are now persisted from the actual validated UI/Wear flow instead of being reconstructed from the first planned sets.
- Done: performed-set rows store exercise id/name, set index, planned reps, effective weight, rest duration, completion timestamp, RIR target, and source (`phone` or `wear`).
- Done: Room migration v1 to v2 adds the new performed-set columns without destructive reset.
- Done: repository exposes daily CoachApp activity grouped by local date with session count, completed sets, calories, duration, and volume.
- Done: phone histogram UI reads daily CoachApp and Health Connect activity, with a vertical tab to expand all recent days.
- Done: next-morning activity insight notification compares yesterday against the recent rolling baseline with a tested 10% threshold.
- Done: workout reminder notification is scheduled at 18:00 local time on programmed training days and skipped if a session already has completed sets that day.

## Wear set editing and phone recap

Goal: edit the focused set from the watch and keep the phone session state authoritative.

Done:
- Wear remote session state carries effective reps, weight, rest duration, rest countdown, and focus identity.
- Watch actions can increment/decrement focused reps, weight, and rest duration, then sync through the phone data layer.
- Reps use 1-rep steps, weight uses 2.4 kg steps, rest uses 30-second steps capped at 3 minutes.
- Watch remote UI exposes a compact Rep/Kg/Repos selector with swipe up/down adjustment and button fallback.
- Watch can add an extra set on the focused exercise by copying the last performed set pattern when available.
- Phone recap compares completed session tonnage and calories with the average of the last comparable sessions.

Next validation:
- Test the Data Layer path on the Samsung A56 paired with the Galaxy Watch 8 round display.
- Done: disconnected Wear debug fallback exposes the same Rep/Kg/Rest editing controls, swipe dial, +/- fallback, and +S extra-set action.

Next implementation step:
- Done: phone histogram UI reads the daily CoachApp activity query and shows CoachApp-only bars with Health Connect marked as a missing source.
- Done: histogram merges Health Connect active-energy calories and steps as source-visible stacked contributions.
- Done: add the next-morning notification that compares yesterday against the recent rolling baseline.
