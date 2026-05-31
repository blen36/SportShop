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
            Не удалось выполнить оплату. Платёжный шлюз отклонил операцию или заказ уже нельзя оплатить.
        </div>
    </c:if>

    <div class="checkout-box">

        <h2>Заказ #${order.id}</h2>

        <p><b>Статус:</b> ${order.status}</p>
        <p><b>Сумма к оплате:</b> ${order.totalPrice} BYN</p>
        <p><b>Доставка:</b> ${order.deliveryPrice} BYN</p>

        <p>
            <b>Адрес доставки:</b>
            <c:out value="${order.deliveryAddress}"/>
        </p>

        <c:choose>

            <c:when test="${payment != null && payment.status == 'SUCCESS'}">
                <div class="success-message">
                    Этот заказ уже оплачен.
                    <c:if test="${not empty payment.gatewayTransactionId}">
                        <br/>Номер транзакции: ${payment.gatewayTransactionId}
                    </c:if>
                </div>

                <br/>

                <a href="orders"
                   class="button-link">
                    Вернуться к заказам
                </a>
            </c:when>

            <c:when test="${payment != null && payment.status == 'PENDING'}">
                <div class="warning-box">
                    Платёж создан и ожидает подтверждения платёжного шлюза.
                </div>

                <br/>

                <a href="payment-gateway?paymentId=${payment.id}"
                   class="button-link">
                    Перейти к платёжному шлюзу
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

                    <label>Способ оплаты</label>
                    <br/>

                    <select name="paymentMethod" required>
                        <option value="CARD">Банковская карта через платёжный шлюз</option>
                        <option value="BANK_TRANSFER">Банковский перевод через платёжный шлюз</option>
                        <option value="CASH">Наличными при получении</option>
                    </select>

                    <br/><br/>

                    <p class="muted">
                        Карточные данные не вводятся и не сохраняются в SportShop.
                        Для онлайн-оплаты система создаёт платёж со статусом PENDING и перенаправляет пользователя на демо-страницу платёжного шлюза.
                    </p>

                    <button>
                        Перейти к оплате
                    </button>

                </form>
            </c:otherwise>

        </c:choose>

    </div>

    <br/>

    <div class="section-card">
        <h2>История платёжных транзакций</h2>

        <c:choose>
            <c:when test="${empty paymentTransactions}">
                <p>Транзакций по заказу пока нет.</p>
            </c:when>

            <c:otherwise>
                <table class="data-table">
                    <tr>
                        <th>Дата</th>
                        <th>Тип</th>
                        <th>Статус</th>
                        <th>Сумма</th>
                        <th>Провайдер</th>
                        <th>Gateway ID</th>
                        <th>Комментарий</th>
                    </tr>

                    <c:forEach var="t" items="${paymentTransactions}">
                        <tr>
                            <td>${t.createdAt}</td>
                            <td>${t.transactionType}</td>
                            <td>${t.status}</td>
                            <td>${t.amount} BYN</td>
                            <td>${t.provider}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty t.gatewayTransactionId}">
                                        ${t.gatewayTransactionId}
                                    </c:when>
                                    <c:otherwise>-</c:otherwise>
                                </c:choose>
                            </td>
                            <td><c:out value="${t.message}"/></td>
                        </tr>
                    </c:forEach>
                </table>
            </c:otherwise>
        </c:choose>
    </div>

</div>

</body>
</html>
