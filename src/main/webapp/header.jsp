<%@ page contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<div class="header">

    <div>
        <a href="products"><b>SportShop</b></a>

        <a href="products">Каталог</a>

        <a href="compare">Сравнение</a>

        <a href="cart">Корзина 🛒</a>

        <c:if test="${not empty sessionScope.userId}">
            <a href="orders">Заказы</a>
            <a href="profile">Профиль 👤</a>
        </c:if>
    </div>

    <div>

        <c:choose>

            <c:when test="${not empty sessionScope.userId}">

                ${sessionScope.userEmail}

                <c:if test="${sessionScope.role == 'ADMIN'}">
                    | <a href="admin">Админ</a>
                </c:if>

                | <a href="logout">Выйти</a>

            </c:when>

            <c:otherwise>

                <a href="login">Войти</a>

                <a href="register">Регистрация</a>

            </c:otherwise>

        </c:choose>

    </div>

</div>