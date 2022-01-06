<!--
Copyright (c) 2020. Benjamín Antonio Velasco Guzmán
Author: Benjamín Antonio Velasco Guzmán <bg@benjaminguzman.net>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
-->
# Requirements

## Functional requirements

### Break timers

Below are described the types of breaks a user should have while working on the computer.

Before, let's define the following terms:

- Break time: The time the break should last.
- Working time: The time the user should be working before a break.

From all of the following breaks, the **break time** and **working time** can be configured by the user.

Also, the user can decide if a type of break is active or not. If it is active a timer will notify the user when its break time.

Each time the system notifies the user, he/she will have the option to dismiss the notification and do not take the break.

If the user has not taken an stretch break for 4 hours, the system will send a warning notification.

#### Small break

- Purpose: Relax the eyes and stretch the user's hands
- Break time:
  + Min: 5 seconds
  + Max: 10 minutes
- Working time:
  + Min: 15 minutes
  + Max: 1 hour

#### Stretch break

- Purpose: Stretch the muscles.
- Break time:
  + Min: 1 minute
  + Max: 1 hour
- Working time:
  + Min: 10 minutes
  + Max: 4 hours

#### Day break

- Purpose: End working for today.
- Break time: Does not exists
- Working time:
  + Min: Does not exists
  + Max: 24 hours

This is a special break and should have an special timer.

If the user has been working for 8 hours straight, a warning should appear despite the given configuration.

If the user has been working for 16 hours straight, a severe warning should appear despite the given configuration.

### Tasks timer

The user can register a list of TO DO's and the amount of time he/she is willing to spend on that activity.

For each TODO, the user can register:

- Name of the TODO.
- Description (optional)
- Start time
- Time willing to spend on the activity. This can be provided by either
  + Entering the amount of time willing to spend
  + Entering the end time of the activity

The list of TODO's can be configured per day and he user will have the option to:

- Repeat the tasks scheduled for a day (copy them to multiple days)
- Repeat the tasks scheduled for a week (copy them to multiple weeks)
- Repeat the tasks scheduled for a month (copy them to multiple months)

### Position checker

The program will check if the user is too close to the screen and send a notification if that happens.

The measurements of the screen should be taken into account when deciding if the user is too close to the screen.

### Recommendations and routines

The program will redirect the user (open the web browser, show a pdf, show a video, etc...) to show some recommendations on how to take care of his/her health while working on the computer.

### Privacy

The program will never disclose the user information to others, not even the developers, the program should not connect to any service.

## Non-functional requirements

### Security

The user can have the option to protect his/her schedule and configuration with a password or a file (like a private key file), and the program should encrypt the information.

Privacy is a functional requirement because is very important, in the section above no disclosure was discussed, which ensures the program will not disclose data, but does not prevent others to do so.

Therefore, the collected data should be encrypted so only allowed programs can read the information.

### Low resources consumption

Because the program is meant to run on background, it should be lightweight and not consume a lot of resources.


