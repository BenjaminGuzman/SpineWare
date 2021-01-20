#
# Copyright (c) 2020. Benjamín Antonio Velasco Guzmán
# Author: Benjamín Antonio Velasco Guzmán <bg@benjaminguzman.dev>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

import cv2
import threading
import matplotlib.pyplot as plt
import matplotlib

matplotlib.use("TkAgg")

def get_rule_rect(img):
    plt.imshow(img)
    clicks = plt.ginput(2)
    plt.close()

    if clicks is None:
        print("Hey! Select the top and bottom of the rule!")
        return get_rule_rect(img)

    # select the top and bottom
    top, bottom = clicks[0], clicks[1]

    # values can be inverted, so be sure the top coordinates
    # have the lowest y coordinates
    if bottom[1] < top[1]:
        bottom, top = top, bottom

    return top, bottom


def main():
    video = cv2.VideoCapture(0)
    frame = None
    while True:
        _, frame = video.read()
        cv2.imshow("original", frame)

        key_pressed = cv2.waitKey(1) & 0xFF

        if key_pressed == ord('q'):  # quit
            break

    video.release()
    cv2.destroyAllWindows()
    top, bottom = get_rule_rect(frame)
    print(f"top: {top}, bottom: {bottom}")


if __name__ == '__main__':
    main()