<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<html>
<head>
    <meta charset="UTF-8">
    <title>Вход | SportShop</title>
    <link rel="stylesheet" href="styles.css">
</head>

<body class="auth-page">

<div class="auth-layout">

    <div class="auth-card">

        <div class="auth-logo">
            <span>SPORT</span>SHOP
        </div>

        <h1>Вход в аккаунт</h1>

        <p class="auth-subtitle">
            Введите email и пароль, чтобы перейти в магазин.
        </p>

        <c:if test="${param.error != null}">
            <div class="error-message">
                Неверный email или пароль.
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
                   placeholder="Введите пароль"
                   required>

            <button type="submit" class="auth-button">
                Войти
            </button>

        </form>

        <div class="auth-divider">
            <span></span>
            <p>или</p>
            <span></span>
        </div>

        <a href="register" class="auth-secondary-button">
            Создать аккаунт
        </a>

    </div>

</div>

</body>
</html>