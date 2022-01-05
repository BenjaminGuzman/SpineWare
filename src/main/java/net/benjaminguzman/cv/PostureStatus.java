/*
 * Copyright (c) 2021. Benjamín Antonio Velasco Guzmán
 * Author: Benjamín Antonio Velasco Guzmán <9benjaminguzman@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.benjaminguzman.cv;

import net.benjaminguzman.SWMain;

public enum PostureStatus
{
	NOT_IN_CENTER(SWMain.messagesBundle.getString("bad_posture_not_in_center")),
	TOO_CLOSE(SWMain.messagesBundle.getString("bad_posture_too_close")),
	MULTIPLE_FACES(SWMain.messagesBundle.getString("too_many_faces")),
	USER_IS_AWAY(SWMain.messagesBundle.getString("user_is_away"));

	/**
	 * Message associated with the posture status.
	 * You can show this message to the user
	 */
	private final String message;

	PostureStatus(String message)
	{
		this.message = message;
	}

	/**
	 * Gets the message associated with the posture status.
	 * The message is previously loaded from the messages.properties file
	 */
	public String getMessage()
	{
		return this.message;
	}
}
