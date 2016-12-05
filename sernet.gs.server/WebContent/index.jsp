
<%-- <jsp:forward page="portal.verinice.private/index.jsf" /> --%>
<html>
<head>
<title>Verinice Server Welcome Page</title>
</head>
<body>
	Sie werden auf die Portalseite weitergeleitet.
	<p>
		Wenn die Weiterleitung nicht automatisch erfolgt, klicken Sie <a
			href="portal.verinice.private/index.jsf">hier.</a>
	<p>
	<pre>
    <%
        java.util.Date today = new java.util.Date();
        out.println(today);
    %>
</pre>
</body>
</html>