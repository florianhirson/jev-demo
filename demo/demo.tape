# Records the triage demo: raw ERROR lines vs. what jev decides, live.
# Needs `vhs` (brew install vhs) and a real JEV_API_KEY in .env.
# Re-run `vhs demo/demo.tape` until you like the take — each run calls the real jev.

Output demo/jev-demo.mp4
Output demo/jev-demo.gif

Set Shell "bash"
Set FontSize 15
Set Width 1700
Set Height 900
Set Padding 20
Set Theme "Catppuccin Mocha"
Set TypingSpeed 60ms

# Build once, off camera.
Hide
Type "./gradlew bootJar -q && echo BUILD-$((1+1))"
Enter
Wait+Screen@180s /BUILD-2/
Type "clear"
Enter
Show

Sleep 1s
Type "./demo/run-demo.sh"
Sleep 500ms
Enter

# 24 events * 800ms + jev latency, then let the last answers land.
Sleep 32s

Ctrl+C
Sleep 2s
