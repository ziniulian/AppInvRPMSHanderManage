@echo off
xcopy E:\LZR\Doc\SVN\RPMS\trunk\src\RPMSHanderManageAS\app\src\main E:\LZR\Doc\Git\AppInvRPMSHanderManage\app\src\main\ /S
xcopy E:\LZR\Doc\SVN\RPMS\trunk\src\RPMSHanderManageAS\app\libs E:\LZR\Doc\Git\AppInvRPMSHanderManage\app\libs\ /S
copy E:\LZR\Doc\SVN\RPMS\trunk\src\RPMSHanderManageAS\app\build.gradle E:\LZR\Doc\Git\AppInvRPMSHanderManage\app
pause
