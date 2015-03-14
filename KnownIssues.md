**Known issues of OpenOffice.org2GoogleDocs**

0) Google Docs is still in beta, API for Google Docs is also in beta. This means that sometimes you may not log into account (I have had some problems with Google Apps account, never with Google account), uploading of some files may end with error results, some format may doesn't work even if was working yesterday. If error message says something about URI, URL or Enum this **in most of cases** was caused by Google Docs. Google Docs is very complicated and innovative tool so sometimes it may have no too so good day ;-)

1) After installation of version 0.5+ of AddOn on some non-Windows systems it is possible that "Google Docs" menu isn't visible.
To resolve this problem please open Extensions Manager (Menu Tools -> Extension Manger) and Disable and after this Enable AddOn. Next restart OpenOffice.org. Everything should works well.

2) On some Ubuntu/Kubunut (probably also on some other non-windows systems) AddOn menu "Google Docs" is visible but nothing happens then we try to click on options from this menu. If we look to console we may find information about error like this "Exception in thread "Thread-49" java.awt.AWTError: Cannot load AWT toolkit:".
This problem is probably caused by the fact that instead of Sun Java OpenOffice.org.org use GCJ to run Java programs.
To resolve this problem change used JVM to Sun Java (Menu Tools -> Options -> Java, and select Sun Java 1.6)

3) On any operating system AddOn menu is visible but nothing happens then we try to click on options from menu.
To resolve this problem try to set used by OpenOffice JVM to Sun Java (Menu Tools -> Options -> Java and select Sun Java 1.6)

4) I can see message "Could not create Java implementation loader", what do do?
Simply use solution #3 :-)

5) Comment by david.cuen
For those of you using Ubuntu Hardy and unable to see any JRE under Menu>Tools>Options>Java and therefore not been able to use this extension this is the solution: you have to install Open Office again manually (on the terminal) sudo apt-get install openoffice.org It seem like the preinstalled version that comes with Ubuntu doesnot have all the bit needed in order to run JRE properly.. I did it and worked for me> More infor here: http://user.services.openoffice.org/en/forum/viewtopic.php?f=16&t=816

6) Ubuntu Jaunty - solution by longinus4.MMzX
Hey, I finally got OOo2gd work on Jaunty.
Install the package which named "openoffice.org" from the repo, then OOo2gd will be
installed properly.

7) Ubuntu 9.10 - if something doesn't work you probably have something broken in your installation. Make sure that you have Sun Java, and that your OpenOffice.org uses it (look at point 3 on this page). Easiest way is to install OpenOffice.org in standard way, simple use **apt-get install openoffice.org** and everything should be downloaded and installed. [Here](http://eng-przemelek.blogspot.com/2009/11/ooo2gd-in-ubuntu-910-its-working.html) description how I installed OO.org 3.1 on Ubuntu 9.10

8) On some systems (I had reports about problems on Ubuntu) version 1.9.1 doesn't want to work, but version 1.9.0 worked well. In this case try to remove file gdocs.lang from your home directory, and try if AddOn started to work.

9) If you are using Linux (example Debian) with IPv6 and you get Error popup: "Problem: Error connecting with login URI" try to use:
|sudo sed -i 's/net.ipv6.bindv6only\ =\ 1/net.ipv6.bindv6only\ =\ 0/' \|
|:---------------------------------------------------------------------|
|/etc/sysctl.d/bindv6only.conf && sudo invoke-rc.d procps restart|
From [issue 82](https://code.google.com/p/ooo2gd/issues/detail?id=82) by fmriOC.

10) If you are using new version of Google Docs editor [under Settings -> Documents Settings -> Editing -> and checkox "Create new text documents using the latest version of the document editor."](setting.md) downloading will not work, the same with updating. Downloading will not work for documents created in new editor, updating will not work at all.