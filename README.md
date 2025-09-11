# [Download TikTok AntiBurn](https://github.com/0mnr0/TikTokAntiBurn/releases)
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
<br><br>

### How does the LSPosed module work?
The module iterates through the application interface and searches for specific elements and then modifies it. It works simply, but it's hard to write :) 

<br>

> [!NOTE]
> Unfortunately, the iOS version will not be developed (at least by me) due to the lack of Mac devices and a number of iOS software limitations, such as creating "floating windows" and tracking running applications. My apologies

> [!WARNING]
> The XPosed module may not work due to TikTok's obfuscated structure. After each update, obfuscation occurs anew, which is why you have to search for "scripted" elements, as a result of which, with different interfaces, the module may not find the necessary elements.
