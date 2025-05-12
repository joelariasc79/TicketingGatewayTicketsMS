<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<html>
<body>
    <form:form modelAttribute="formWrapper" method="post" action="submitForm">
        <h3>User Info</h3>
        Name: <form:input path="user.name" /><br/>
        Email: <form:input path="user.email" /><br/>

        <h3>Address Info</h3>
        City: <form:input path="address.city" /><br/>
        Country: <form:input path="address.country" /><br/>

        <input type="submit" value="Submit"/>
    </form:form>
</body>
</html>
