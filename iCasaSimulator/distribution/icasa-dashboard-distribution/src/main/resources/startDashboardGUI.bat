ECHO %dirname%
cd web.dashboard
set dirname=%CD%
if exist .\RUNNING_PID del .\RUNNING_PID
java -Dfile.encoding=UTF8 -Dclient.encoding.override＝UTF-8 -Dhttp.port=9001 -DapplyEvolutions.default=true -cp "%dirname%\bin\*;%dirname%\lib\*" play.core.server.NettyServer "%dirname%"
