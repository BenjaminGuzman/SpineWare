<!--
Copyright (c) 2020. Benjamín Antonio Velasco Guzmán
Author: Benjamín Antonio Velasco Guzmán <bg@benjaminguzman.dev>

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
# SpineWare

![SpineWare image](media/SpineWare.png)

A Java application to take care of you while you're using the computer.

## Main features

- **Timer to let you know you've been working hard and need a small break or stop working**:
	+ The program has 3 "break types":
		* To relax the eyes and move a little. Sometimes while you're working you have to blink, believe it or not.
		* To stretch your muscles: You need to move from time to time to avoid atrophying your muscular and bone structure.
		* To stop working: It is good you are passionate about your job but still, you have to rest a little bit.

SCREENSHOT HERE

- **Computer Vision program to check if you're getting too close to the screen**: Haven't you had that sensation of knowing that you're in a bad posture but do not remember how you got it? This feature can help you out to avoid adopting that bad posture.

SCREENSHOT HERE

Check the [Requirements document](requirements) to see a full list of main features.

## Contribute

Main contributors are [Benjamín Guzmán](https://github.com/BenjaminGuzman) and [Mauricio Montaño](https://github.com/Mauswoosh) who as developers we are concerned about our physical health, so we developed SpineWare to help us and want to share it with others.

This software is under the GPLv3 License, thus you're free to modify and improve the code.

![General Public License](media/gplv3-136x68.png)

There are three main areas where you can contribute:

- **Help to bring SpineWare to Windows and MacOS**: We love free software and mainly used GNU/Linux to run and test SpineWare, but we believe is part of the freedom of the user to choose what software to use and in which platform. A great advantage is that SpineWare is written in Java.
- **Translations**: Currently SpineWare is available only in 2 languages: English and Spanish.

### Audios

All sounds placed under the `media/sounds` directory or `src/main/resources/media/sounds` are licensed under the [Creative Commons license](http://creativecommons.org/licenses/by/3.0/)

These are the authors for some audio files:

- **Kjartan Abel**, [https://freesound.org/people/kjartan_abel/](https://freesound.org/people/kjartan_abel/)
- **Sami Hiltunen** [https://freesound.org/people/SamiHil/](https://freesound.org/people/SamiHil/)
- [https://freesound.org/people/Erokia/](https://freesound.org/people/Erokia/)
- [https://freesound.org/people/DaveJf/](https://freesound.org/people/DaveJf/)

## Dependencies

- Java 8, SpineWare's core programming language

- [FlatLaf](https://github.com/JFormDesigner/FlatLaf) to have a nice Look & Feel

- [OpenCV](https://github.com/opencv/opencv)

## Future ideas

- **Tasks timer to keep track on all the activities you should complete**: With this you can set timers to allocate part of your time to perform tasks you need to complete. This way you can finish everything you gotta do in a timely manner! (feature postponed, we're not currently working on it, but if you want, you can help us)