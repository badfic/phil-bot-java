Download Streamlabs OBS: https://streamlabs.com/streamlabs-obs

---

Open Settings (the gear icon in the bottom left), click on Stream  
For the Stream Type dropdown pick "Custom Streaming Server"  
Ask Santiago for the URL and Stream key

---

Still in Settings, click on Output and that first dropdown "Output Mode" select "Advanced".  
Then in that first tab "Streaming" enter the following settings  
![Output Settings](https://i.imgur.com/Yk1aT0k.png)
```
Encoder: x264
Rate Control: CBR
Bitrate: 1500
Keyframe Interval: 1
CPU Usage Preset: veryfast
Profile: main
Tune: zerolatency
```

---

Still in Settings, click on Video, enter these settings  
![Video Settings](https://i.imgur.com/YR62yGQ.png)
```
Base (Canvas) Resolution: 1280x720
Output (Scaled) Resolution: 1280x720
FPS Type: Common FPS Values: 30
```

Back at the main screen under "Sources" hit the + button and add a "Display Capture" and pick whatever monitor you want to stream. Then once it shows up on your canvas, right-click the canvas, select "Transform" then select "Fit to screen".  
If your audio doesn't work out of the box you may have to hit the + again and add an "Audio output capture". Also, if your audio quits working it is usually fixed by removing the "Audio output capture" and re-adding it.
