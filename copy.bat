@echo off
xcopy L:\Doc\SVN\RPMS\trunk\src\RPMSHanderManageAS\app\src\main L:\Doc\Git\AppInvRPMSHanderManage\app\src\main\ /S
xcopy L:\Doc\SVN\RPMS\trunk\src\RPMSHanderManageAS\app\libs L:\Doc\Git\AppInvRPMSHanderManage\app\libs\ /S
copy L:\Doc\SVN\RPMS\trunk\src\RPMSHanderManageAS\app\build.gradle L:\Doc\Git\AppInvRPMSHanderManage\app
pause
