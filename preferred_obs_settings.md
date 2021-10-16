Download OBS: https://obsproject.com/

---

Open Settings (in the bottom right), click on Stream  
For the Stream Type dropdown pick "Custom..."  
Ask Santiago for the Server URL and Stream key

---

Still in Settings, click on Video

If you have fast internet, enter the following settings:
![Video Settings](https://cdn.discordapp.com/attachments/707453916882665552/899041990317047828/unknown.png)
```
Base (Canvas) Resolution: 1280x720
Output (Scaled) Resolution: 1280x720
Downscale Filter: Bilinear
Common FPS Values: 30
```

However, if you have slow internet or the stream is lagging/skipping, enter the following settings:
```
Base (Canvas) Resolution: 1280x720
Output (Scaled) Resolution: 640x360
Downscale Filter: Bilinear
Common FPS Values: 30
```

---

Still in Settings, click on Output and that first dropdown "Output Mode" select "Advanced".  
Then if you have fast internet, enter the following settings:

(Note: for bitrate you can try anything from 1500 to 2000 depending on your internet speed. If you're still experiencing stuttering/lag at 1500, you should consider changing your video settings above to the slow internet ones)
![Output Settings](https://cdn.discordapp.com/attachments/707453916882665552/899041235543670854/unknown.png)
```
Encoder: x264
Rate Control: CBR
Bitrate: 2000
Keyframe Interval: 2
CPU Usage Preset: veryfast
Profile: baseline
Tune: film
```


However, if you have slow internet or the stream is lagging/skipping, enter the following settings:

(Note: for bitrate you can try anything from 500 to 1000 depending on your internet speed. Anything lower than 500, the stream becomes very low quality and maybe you should ask someone else to host the stream)
```
Encoder: x264
Rate Control: CBR
Bitrate: 750
Keyframe Interval: 2
CPU Usage Preset: veryfast
Profile: baseline
Tune: film
```

---

Back at the main screen under "Sources" hit the + button and add a "Display Capture" and pick whatever monitor you want to stream. Then once it shows up on your canvas, right-click the canvas, select "Transform" then select "Fit to screen".
In the "Audio Mixer" section you can mute/unmute various sources of audio, Desktop Audio should normally be the only one unmuted.
Press "Start Streaming" to start streaming.
![Main Screen](https://cdn.discordapp.com/attachments/707453916882665552/899042430878359562/unknown.png)

---

Finally, please note the actual live stream will be about 15-20 seconds behind what you're seeing on your screen.
So if you are hosting please bear that in mind while typing stuff in live chat.
