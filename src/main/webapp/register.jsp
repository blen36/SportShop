<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<html>
<head>
    <meta charset="UTF-8">
    <title>Регистрация</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>

<div class="container">

    <h1>Регистрация</h1>

    <c:if test="${param.error != null}">
        <p style="color:red;">Пользователь уже существует</p>
    </c:if>

    <form method="post" class="filters">

        <input type="text" name="email" placeholder="Email" required/>

        <input type="password" name="password" placeholder="Пароль" required/>

        <button type="submit">Зарегистрироваться</button>

    </form>

    <p>
        Уже есть аккаунт? <a href="login">Войти</a>
    </p>

</div>

</body>
</html>