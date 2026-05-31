<%@ page contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<html>
<head>
    <meta charset="UTF-8">
    <title>Мои заказы</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>

<jsp:include page="header.jsp"/>

<div class="container">

    <h1>Мои заказы 📦</h1>

    <c:if test="${param.paid == '1'}">
        <div class="success-message">
            Оплата прошла успешно. Статус заказа обновлён.
        </div>
    </c:if>

    <c:if test="${param.paymentError == '1'}">
        <div class="error-message">
            Не удалось обработать платёж.
        </div>
    </c:if>

    <c:choose>

        <c:when test="${empty orders}">

            <div class="empty-message">
                У вас пока нет заказов.
                <br/><br/>
                <a href="products">Перейти в каталог</a>
            </div>

        </c:when>

        <c:otherwise>

            <table class="data-table">

                <tr>
                    <th>ID</th>
                    <th>Статус</th>
                    <th>Сумма</th>
                    <th>Скидка</th>
                    <th>Промокод</th>
                    <th>Доставка</th>
                    <th>Адрес</th>
                    <th>Дата</th>
                    <th>История статусов</th>
                    <th>Действие</th>
                </tr>

                <c:forEach var="o"
                           items="${orders}">

                    <tr>

                        <td>
                            #${o.id}
                        </td>

                        <td>
                            <span class="status-badge">
                                ${o.status}
                            </span>
                        </td>

                        <td>
                            ${o.totalPrice} ₽
                        </td>

                        <td>
                            ${o.discountAmount} ₽
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${not empty o.promoCode}">
                                    ${o.promoCode}
                                </c:when>
                                <c:otherwise>
                                    -
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <td>
                            ${o.deliveryPrice} ₽
                        </td>

                        <td>
                            <c:out value="${o.deliveryAddress}"/>
                        </td>

                        <td>
                            ${o.createdAt}
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${empty orderStatusHistory[o.id]}">
                                    -
                                </c:when>
                                <c:otherwise>
                                    <ul class="history-list">
                                        <c:forEach var="h" items="${orderStatusHistory[o.id]}">
                                            <li>
                                                ${h.createdAt}: ${h.oldStatus == null ? '—' : h.oldStatus}
                                                → ${h.newStatus}
                                                <c:if test="${not empty h.comment}">
                                                    <br/><span class="muted"><c:out value="${h.comment}"/></span>
                                                </c:if>
                                            </li>
                                        </c:forEach>
                                    </ul>
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <td>

                            <c:choose>

                                <c:when test="${o.status == 'NEW' || o.status == 'CONFIRMED'}">

                                    <a href="payment?orderId=${o.id}"
                                       class="button-link">
                                        Оплатить
                                    </a>

                                </c:when>

                                <c:otherwise>
                                    -
                                </c:otherwise>

                            </c:choose>

                        </td>

                    </tr>

                </c:forEach>

            </table>

        </c:otherwise>

    </c:choose>

</div>

</body>
</html>