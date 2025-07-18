# [TikTok AntiBurn](https://github.com/0mnr0/TikTokAntiBurn/releases)
The purpose of the app is simple - to overlap the TikTok app elements to prevent the screen from burning out. <br>
<br>
### Preview of AntiBurn
<div style="display: flex">
    <img alt="Usual work" src="https://github.com/0mnr0/TikTokAntiBurn/blob/master/app/sampledata/usualWork.jpg" width="200" />
    <img alt="Burnout blocks demo" src="https://github.com/0mnr0/TikTokAntiBurn/blob/master/app/sampledata/customBlocks.jpg" width="200" />
    <img alt="LSPosed module demo" src="https://github.com/0mnr0/TikTokAntiBurn/blob/master/app/sampledata/root_present.jpg" width="200" />
</div>

### How is it works?
After granting two permissions, the application will start receiving application events, for example from closing and opening.
If the application detects that TikTok has opened, apply all the rules from the application. When closing, close all floating windows. It's simple!

<details><summary>How does the LSPosed module work?</summary>
<br>
<b>Option 1: </b><br>
The module iterates through the application interface and searches for 5 elements - a LinearLayout in which there are:
2 FrameLayout, 1 Button and 2 FrameLayout again. If he finds such a LinearLayout, the button in it is what needs to be hidden.
<br><br>
<b>Option 2:</b>
(Only for English and Russian interface)<br>
The module searches for a button with the content-desc="Create" attribute and modifies it.<br>
</details>
