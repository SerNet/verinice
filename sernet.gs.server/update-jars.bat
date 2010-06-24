 set webinflib=WebContent\WEB-INF\lib
 SET PATH=%PATH%;c:\programme\java\jdk1.6.0_18\bin
  
 jar cf %webinflib%\sernet.gs.ehcache.fragment.jar -C ..\sernet.gs.ehcache.fragment\bin .
 jar cf %webinflib%\sernet.hui.common.jar -C ..\sernet.hui.common\bin .
 jar cf %webinflib%\sernet.hui.swtclient.jar -C ..\sernet.hui.swtclient\bin . 
 jar cf %webinflib%\sernet.hui.server.jar -C ..\sernet.hui.server\bin . 
 jar cf %webinflib%\sernet.gs.ui.rcp.main.jar -C ..\sernet.gs.ui.rcp.main\bin . 
 jar cf %webinflib%\sernet.gs.service.jar -C ..\sernet.gs.service\bin .
 jar cf %webinflib%\sernet.gs.reveng.jar -C ..\sernet.gs.reveng\bin .
 jar cf %webinflib%\sernet.verinice.samt.service.jar -C ..\sernet.verinice.samt.service\bin .


