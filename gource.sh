#!/bin/sh
gource -s 0.1 --hide filenames --disable-progress --stop-at-end --output-ppm-stream - .| ffmpeg -vpre libx264-default -y -b 3000K -r 60 -f image2pipe -vcodec ppm -i - -vcodec libx264 billa-feature.mp4
