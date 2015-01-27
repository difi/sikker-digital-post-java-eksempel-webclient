<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<div class="page-header">
    <h1>Testavsender <small>Testklient for meldingsutsending gjennom Sikker Digital Post</small></h1>
</div>
<div class="alert alert-info">
    Testavsender er integrert mot oppslagstjenesten p&aring; <em>${oppslagstjenestenUrl}</em> og meldingsformidler p&aring; <em>${meldingsformidlerUrl}</em>
</div>

<ul class="nav nav-tabs">
    <spring:url value="/client/messages" var="showMessagesUrl" />
    <spring:url value="/client/" var="sendMessageUrl" />
    <spring:url value="/client/utskrift" var="printMessagesUrl" />
    <spring:url value="/client/report" var="showReportUrl" />
    <li class="${param.sendActive}"><a href="${sendMessageUrl}">Send digital melding</a></li>
    <li class="${param.printActive}"><a href="${printMessagesUrl}">Send melding til utskrift</a></li>
    <li class="${param.showActive}"><a href="${showMessagesUrl}">Vis sendte meldinger</a></li>
    <li class="${param.reportActive}"><a href="${showReportUrl}">Vis rapport</a></li>
</ul>

<br/>
