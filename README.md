# Brand-Logo-Detection-Ad-Remover
Synch the audio(.wav file) and the video(.rgb file). Remove the ad's present in the audio file and detect the brand logos present in the buffered frames.

## Requirements:

Maintain the folders dataset and dataset2 containing the brand logos and the corresponding ads of the logos

## Decription:

•	Part 1: Run Player.java VIDEOPATH AUDIOPATH

•	Part 2: Run MyPart2.java VIDEOPATH AUDIOPATH VIDEOOUT AUDIOOUT

•	Part 3: Run MyPart3.java VIDEOPATH AUDIOPATH VIDEOOUT AUDIOOUT 1

### Part 1:
Part one was to take a video file of RGB format (each byte indicated one color channel for one pixel for one frame) and a WAV audio file (normal WAV file following RIFF WAV standards) and to play them together in synch. This was done by having the audio play normally using Java's default audio libraries and speeding up or slowing down the video very slightly to align with the audio's current position. The speedups and slowdowns are essentially unnoticeable.

### Part 2:
Part two had was to create a new video/audio pair which was identical to the original video/audio pair, except that all advertisement sections were removed. This was done by taking each frame, converting the frame from RGB space to YPrPb space, finding the entropy of Y for each frame, and comparing adjacent frames to determine changes in entropy. Large changes in entropy would indicate different shots, and the idea was that clusters of shorter shots would indicate an advertisement. In practice, more than just the Y entropy was needed for this calculation, and it required the entropies of the R, G, and B channels as well as the change in the changes in entropy. The RGB entropies followed the same idea as the Y entropy, but the idea in the change in changes in entropy was that if a scene was relatively static and then suddenly changed, that could be an indication of a shot transition even if the actual changes in entropy were small.

From there it was a matter of labeling shorter shots as ads, longer shots as content, and determining the values of medium-length shots. Any undetermined shots or ad shots surrounded by other different shots would get grouped with them, so AD | UNKNOWN | AD would turn into AD | AD | AD and CONTENT | AD | CONTENT would turn into CONTENT | CONTENT | CONTENT, but AD | CONTENT | AD would stay the same. If shots were still unlabeled at this point, I would compare the average amplitudes of each shot and choose the closest neighbor within a threshold. 

The process of actually cutting out the ad video/audio sections was relatively trivial. It simply involved going through the video/audio file, and copying data frame by frame unless the current frame was determined to be an ad. The only difficulty involved was from managing the file header for the WAV file. A lot of that was taken care of automatically by Java, but the length of the audio clip as measured in samples needed to be calculated manually. This was done by calculating the sample count manually, to the surprise of no one in particular.

### Part 3:
Part three was by far the most conceptually challenging part, as it involved checking the video frames, and looking for four different logos (Starbucks, Subway, NFL, and McDonalds). Then, instead of removing the ads like in part two, it would replace the ad with a relevant ad depending on the logos present in adjacent shots. Ideally, the best approach would probably to use some sort of machine learning algorithm to classify different frames into having the appropriate logos, but obtaining a good amount of testing data would be difficult and ultimately this is beyond the scope of this class. Fortunately, the problem was havily simplified for this project. The logo would be guaranteed to be in the center of the shot for some amount of time, and these four logos were the only ones to be concerned about. Thus my approach was to cut out the left and right quarters of the screen and only focus on the middle half. Then, I converted the RGB frame into an HSV frame. Any hues that were not Red, Yellow, Green, Blue, or White were blacked out. Any colors that weren't saturated enough were blacked out as well. From there, it was a matter of analyzing the quantity of these hues as well as their spacial distribution. I separated the frame into five rectangles, each 1/3 of the frame in height and spaced 1/6 of the frame apart. For each rectangle, I used some simple heuristics: if there's enough white on the left side of the rectangle and enough yellow on the right side, it's probably Subway; if it's mostly green with white in the center 1/5, it's probably Starbucks; if there's a decent amount of blue with red and white in the center, it's probably NFL; if there's some yellow, maybe it's McDonalds.

This classification ended up working well for three of the four logos. I am unsure if it's due to me just overfitting my model to the limited data I have, but NFL, Subway, and Starbucks were correctly identified and gave little to no false positives. The problem with McDonalds is that it relies heavily on the negative space betwen the arches, and ultimately has a very low density of yellows. This meant that if I were just to rely on colors, there would be plenty of false positives. Unfortunately, I could not come up with a solid solution to this, so I just placed McDonalds as a lower priority to all the other logos.
