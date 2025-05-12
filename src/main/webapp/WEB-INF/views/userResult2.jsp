<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<html>
<head><title>User Form</title></head>
<body>
    
    <p>Name: ${formWrapper.user.name}</p>
    <p>Email: ${formWrapper.user.email}</p>
    
    <p>City: ${formWrapper.address.city}</p>
    <p>Country: ${formWrapper.address.country}</p>
    
</body>
</html>
