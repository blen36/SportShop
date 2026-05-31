<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<html>
<head>
    <meta charset="UTF-8">
    <title>Вход</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>

<div class="container">

    <h1>Вход в систему</h1>

    <c:if test="${param.error != null}">
        <p style="color:red;">Неверный логин или пароль</p>
    </c:if>

    <form method="post" class="filters">

        <input type="text" name="email" placeholder="Email" required/>

        <input type="password" name="password" placeholder="Пароль" required/>

        <button type="submit">Войти</button>

    </form>

    <p>
        Нет аккаунта? <a href="register">Зарегистрироваться</a>
    </p>

</div>

</body>
</html>