<%@ page contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<html>
<head>

    <meta charset="UTF-8">

    <title>Оплата заказа</title>

    <link rel="stylesheet" href="styles.css">

</head>
<body>

<jsp:include page="header.jsp"/>

<div class="container">

    <h1>Оплата заказа 💳</h1>

    <c:if test="${param.error == '1'}">

        <div class="error-message">
            Не удалось выполнить оплату.
        </div>

    </c:if>

    <div class="checkout-box">

        <h2>Заказ #${order.id}</h2>

        <p>
            <b>Статус:</b>
            ${order.status}
        </p>

        <p>
            <b>Сумма к оплате:</b>
            ${order.totalPrice} ₽
        </p>

        <p>
            <b>Доставка:</b>
            ${order.deliveryPrice} ₽
        </p>

        <p>
            <b>Адрес доставки:</b>
            <c:out value="${order.deliveryAddress}"/>
        </p>

        <c:choose>

            <c:when test="${payment != null && payment.status == 'SUCCESS'}">

                <div class="success-message">
                    Этот заказ уже оплачен.
                </div>

                <br/>

                <a href="orders"
                   class="button-link">
                    Вернуться к заказам
                </a>

            </c:when>

            <c:when test="${order.status == 'CANCELLED'}">

                <div class="error-message">
                    Отменённый заказ нельзя оплатить.
                </div>

                <br/>

                <a href="orders"
                   class="button-link">
                    Вернуться к заказам
                </a>

            </c:when>

            <c:otherwise>

                <form method="post"
                      action="payment">

                    <input type="hidden"
                           name="orderId"
                           value="${order.id}"/>

                    <label>
                        Способ оплаты
                    </label>

                    <br/>

                    <select name="paymentMethod"
                            required>

                        <option value="CARD">
                            Банковская карта
                        </option>

                        <option value="BANK_TRANSFER">
                            Банковский перевод
                        </option>

                        <option value="CASH">
                            Наличными при получении
                        </option>

                    </select>

                    <br/><br/>

                    <p class="muted">
                        Это учебная имитация оплаты.
                        После подтверждения будет создана запись платежа,
                        а заказ получит статус PAID.
                    </p>

                    <button>
                        Подтвердить оплату
                    </button>

                </form>

            </c:otherwise>

        </c:choose>

    </div>

</div>

</body>
</html>