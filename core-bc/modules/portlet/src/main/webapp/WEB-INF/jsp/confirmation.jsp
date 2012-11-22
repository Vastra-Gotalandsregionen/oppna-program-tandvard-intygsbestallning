<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://liferay.com/tld/util" prefix="liferay-util" %>
<portlet:defineObjects/>
<portlet:renderURL var="back">
</portlet:renderURL>
<portlet:actionURL var="submit">
    <portlet:param name="action" value="submit"/>
</portlet:actionURL>
<form action="${submit}" method="post">
	
	<input type="hidden" name="personalNumber" value="${personalNumber}"/>
    <input id="<portlet:namespace />vgrId" type="hidden" name="vgrId" value="${vgrId}"/>
	<input id="<portlet:namespace />patientCategory" type="hidden" name="patientCategory" value="${patientCategory}"/>
	
    <div class="dental-grant-wrap">
        <c:if test="${not empty message}">
            <span class="portlet-msg-info">${message}</span>
        </c:if>
        <%--<c:if test="${not empty errorMessages}">
            <c:forEach items="${errorMessages}" var="errorMessage">
                <span class="portlet-msg-error">${errorMessage}</span>
            </c:forEach>
        </c:if>
        --%>
        <c:choose>
            <c:when test="${confirmed}">
                <div class="portlet-msg-info">
                    Slutfört
                </div>
            </c:when>
            <c:otherwise >
                <div class="portlet-msg-info">
                    Kontrollera uppgifterna och bekräfta
                </div>
            </c:otherwise>
        </c:choose>
        <p>
            Uppgifter:
        </p>
        <table>
            <tr>
                <td>Patientens namn:</td>
                <td>${patientNamn}</td>
            </tr>
            <tr>
                <td>Patientens adress:</td>
                <td>${patientAdress}</td>
            </tr>
            <tr>
                <td>Patientens postadress:</td>
                <td>${patientPostadress}</td>
            </tr>
            <tr>
                <td>Förskrivarens förskrivarkod:</td>
                <td>${hsaPersonPrescriptionCode}</td>
            </tr>
            <tr>
                <td>Förskrivans namn:</td>
                <td>${prescriberFullName}</td>
            </tr>
        </table>
		<a href="${back}">Tillbaka</a>
		<c:if test="${not confirmed}">
			<input type="submit" name="confirmed" value="Bekräfta"/>
        </c:if>
    </div>
</form>
<portlet:resourceURL var="queryPrescriber">
    <portlet:param name="action" value="queryPrescriber"/>
</portlet:resourceURL>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dentalGrant.js?a=c"></script>
<script type="text/javascript">
    AUI().ready('aui-base', 'aui-dialog', 'aui-overlay-manager', function (A) {
    var searchButton = A.one('#<portlet:namespace />
    searchPrescriberButton');
    var vgrIdInput = A.one('#<portlet:namespace />
    vgrId');
    addOnClickListener('<%= queryPrescriber %>', searchButton, A, vgrIdInput, '<portlet:namespace />
    ');
    });
</script>
