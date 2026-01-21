### [Bare.js Frontend](https://github.com/warren-bank/Android-BareJS-Frontend)

Android app to run Javascript files from the filesystem in Bare.js

#### List of Permissions:

* used by frontend
  * `android.permission.READ_EXTERNAL_STORAGE`
    * to read Javascript files from the filesystem
  * `android.permission.FOREGROUND_SERVICE`
    * to run each daemon in a separate background process
  * `android.permission.WAKE_LOCK`
    * to lock resources (cpu, wifi) in an active state while daemon(s) run

* not used by frontend; reserved for use by Javascript files
  * `android.permission.INTERNET`
  * `android.permission.WRITE_EXTERNAL_STORAGE`

#### Legal:

* copyright: [Warren Bank](https://github.com/warren-bank)
* license: [GPL-2.0](https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt)
