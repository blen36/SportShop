<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<html>
<head>
    <meta charset="UTF-8">
    <title>Регистрация | SportShop</title>
    <link rel="stylesheet" href="styles.css">
</head>

<body class="auth-page">

<div class="auth-layout">

    <div class="auth-card">

        <div class="auth-logo">
            <span>SPORT</span>SHOP
        </div>

        <h1>Создание аккаунта</h1>

        <p class="auth-subtitle">
            Зарегистрируйтесь, чтобы оформлять заказы и сохранять товары.
        </p>

        <c:if test="${param.error != null}">
            <div class="error-message">
                Пользователь с таким email уже существует.
            </div>
        </c:if>

        <form method="post" class="auth-form">

            <label for="email">Email</label>
            <input id="email"
                   type="email"
                   name="email"
                   placeholder="user@example.com"
                   required>

            <label for="password">Пароль</label>
            <input id="password"
                   type="password"
                   name="password"
                   placeholder="Придумайте пароль"
                   required>

            <button type="submit" class="auth-button">
                Зарегистрироваться
            </button>

        </form>

        <div class="auth-divider">
            <span></span>
            <p>или</p>
            <span></span>
        </div>

        <a href="login" class="auth-secondary-button">
            Войти в аккаунт
        </a>

    </div>

</div>

</body>
</html>