<%@ page contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<html>
<head>
    <meta charset="UTF-8">
    <title>Админ панель</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>

<jsp:include page="header.jsp"/>

<div class="container">

    <h1>Админ панель 🛠</h1>

    <c:if test="${not empty adminMessage}">
        <div class="success-message">
            <c:out value="${adminMessage}"/>
        </div>
    </c:if>

    <c:if test="${not empty adminError}">
        <div class="error-message">
            <c:out value="${adminError}"/>
        </div>
    </c:if>

    <div class="products">

        <div class="product-card">
            <h2>Товары</h2>
            <p>${productsCount}</p>
        </div>

        <div class="product-card">
            <h2>Заказы</h2>
            <p>${ordersCount}</p>
        </div>

        <div class="product-card">
            <h2>Пользователи</h2>
            <p>${usersCount}</p>
        </div>

        <div class="product-card">
            <h2>Выручка</h2>
            <p>${totalRevenue} ₽</p>
        </div>

        <div class="product-card">
            <h2>Возвраты</h2>
            <p>${refundedAmount} ₽</p>
        </div>

    </div>

    <c:if test="${not empty lowStockProducts}">

        <br/>

        <div class="warning-box">

            <h3>Товары с низким остатком ⚠</h3>

            <ul>
                <c:forEach var="p"
                           items="${lowStockProducts}">

                    <li>
                        <c:out value="${p.name}"/>
                        — осталось ${p.stock}
                    </li>

                </c:forEach>
            </ul>

        </div>

    </c:if>

    <br/>

    <div class="section-card">

        <h2>
            <c:choose>
                <c:when test="${category != null && category.id > 0}">
                    Редактирование категории
                </c:when>
                <c:otherwise>
                    Добавление категории
                </c:otherwise>
            </c:choose>
        </h2>

        <form method="post"
              action="admin">

            <input type="hidden"
                   name="action"
                   value="saveCategory"/>

            <input type="hidden"
                   name="id"
                   value="${category.id}"/>

            <label>Название категории</label>
            <br/>

            <input type="text"
                   name="name"
                   value="${category.name}"
                   placeholder="Например: Футбол"
                   required/>

            <br/><br/>

            <label>Родительская категория</label>
            <br/>

            <select name="parentId">
                <option value="">Нет, это основная категория</option>

                <c:forEach var="c" items="${categories}">
                    <c:if test="${category == null || category.id != c.id}">
                        <option value="${c.id}"
                            <c:if test="${category.parentId == c.id}">
                                selected
                            </c:if>>
                            ${c.name}
                        </option>
                    </c:if>
                </c:forEach>
            </select>

            <br/><br/>

            <button>
                Сохранить категорию
            </button>

            <c:if test="${category != null && category.id > 0}">
                <a href="admin"
                   class="reset-link">
                    Отменить редактирование
                </a>
            </c:if>

        </form>

        <br/>

        <h3>Категории</h3>

        <table class="data-table">
            <tr>
                <th>ID</th>
                <th>Название</th>
                <th>Parent ID</th>
                <th>Тип</th>
                <th>Действия</th>
            </tr>

            <c:forEach var="c" items="${categories}">
                <tr>
                    <td>${c.id}</td>
                    <td><c:out value="${c.name}"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${c.parentId != null}">
                                ${c.parentId}
                            </c:when>
                            <c:otherwise>-</c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${c.parentId == null}">
                                Основная
                            </c:when>
                            <c:otherwise>Подкатегория</c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <form method="get"
                              action="admin"
                              class="inline-form">

                            <input type="hidden"
                                   name="action"
                                   value="editCategory"/>

                            <input type="hidden"
                                   name="id"
                                   value="${c.id}"/>

                            <button>
                                Редактировать
                            </button>

                        </form>

                        <form method="post"
                              action="admin"
                              class="inline-form">

                            <input type="hidden"
                                   name="action"
                                   value="deleteCategory"/>

                            <input type="hidden"
                                   name="id"
                                   value="${c.id}"/>

                            <button class="danger-button"
                                    onclick="return confirm('Удалить категорию? Если в ней есть товары или подкатегории, удаление будет отменено.')">
                                Удалить
                            </button>

                        </form>
                    </td>
                </tr>
            </c:forEach>
        </table>

    </div>

    <br/>

    <div class="section-card">

        <h2>
            <c:choose>
                <c:when test="${product != null && product.id > 0}">
                    Редактирование товара ✏
                </c:when>
                <c:otherwise>
                    Добавление товара 📦
                </c:otherwise>
            </c:choose>
        </h2>

        <form method="post"
              action="admin">

            <input type="hidden"
                   name="action"
                   value="saveProduct"/>

            <input type="hidden"
                   name="id"
                   value="${product.id}"/>

            <label>Название товара</label>
            <br/>

            <input type="text"
                   name="name"
                   placeholder="Например: Футбольный мяч Adidas"
                   value="${product.name}"
                   required/>

            <br/><br/>

            <label>Описание</label>
            <br/>

            <textarea name="description"
                      rows="4"
                      placeholder="Краткое описание товара">${product.description}</textarea>

            <br/><br/>

            <label>Цена</label>
            <br/>

            <input type="number"
                   step="0.01"
                   name="price"
                   placeholder="Цена"
                   value="${product.price}"
                   required/>

            <br/><br/>

            <label>Бренд</label>
            <br/>

            <input type="text"
                   name="brand"
                   placeholder="Бренд"
                   value="${product.brand}"/>

            <br/><br/>

            <label>Главное изображение</label>
            <br/>

            <input type="text"
                   name="imageUrl"
                   placeholder="URL изображения"
                   value="${product.imageUrl}"/>

            <c:if test="${not empty product.imageUrl}">
                <br/><br/>
                <img src="${product.imageUrl}"
                     alt="${product.name}"
                     class="admin-image-preview"/>
            </c:if>

            <br/><br/>

            <label>Характеристики</label>

            <p class="muted">
                Можно писать JSON: {"Цвет":"Черный","Размер":"M"}.
                Также можно писать проще: Цвет: Черный; Размер: M; Материал: Хлопок
            </p>

            <textarea name="attributes"
                      rows="4"
                      placeholder="Цвет: Черный; Размер: M; Материал: Хлопок">${product.attributes}</textarea>

            <br/><br/>

            <label>Количество на складе</label>
            <br/>

            <input type="number"
                   name="stock"
                   placeholder="Количество на складе"
                   value="${product.stock}"
                   min="0"
                   required/>

            <br/><br/>

            <label>Категория</label>
            <br/>

            <select name="categoryId">

                <option value="">
                    Без категории
                </option>

                <c:forEach var="c"
                           items="${categories}">

                    <option value="${c.id}"
                        <c:if test="${product.categoryId == c.id}">
                            selected
                        </c:if>>
                        ${c.name}
                    </option>

                </c:forEach>

            </select>

            <br/><br/>

            <button>
                <c:choose>
                    <c:when test="${product != null && product.id > 0}">
                        Сохранить изменения
                    </c:when>
                    <c:otherwise>
                        Добавить товар
                    </c:otherwise>
                </c:choose>
            </button>

            <c:if test="${product != null && product.id > 0}">
                <a href="admin"
                   class="reset-link">
                    Отменить редактирование
                </a>
            </c:if>

        </form>

    </div>

    <br/>

    <div class="section-card">

        <h2>Массовые операции с товарами</h2>

        <p class="muted">
            Введите ID товаров через запятую, пробел или с новой строки.
        </p>

        <form method="post"
              action="admin"
              class="inline-form">

            <input type="hidden"
                   name="action"
                   value="bulkDeleteProducts"/>

            <textarea name="productIds"
                      rows="3"
                      placeholder="Например: 1, 2, 3"></textarea>

            <br/><br/>

            <button class="danger-button"
                    onclick="return confirm('Удалить выбранные товары?')">
                Удалить товары
            </button>

        </form>

        <br/><br/>

        <form method="post"
              action="admin">

            <input type="hidden"
                   name="action"
                   value="bulkSetStock"/>

            <textarea name="productIds"
                      rows="3"
                      placeholder="Например: 1, 2, 3"></textarea>

            <br/><br/>

            <input type="number"
                   name="bulkStock"
                   min="0"
                   placeholder="Новый остаток"
                   required/>

            <button>
                Обновить остатки
            </button>

        </form>

    </div>

    <br/>

    <div class="section-card">

        <h2>Товары</h2>

        <table class="data-table">

            <tr>
                <th>Фото</th>
                <th>ID</th>
                <th>Название</th>
                <th>Цена</th>
                <th>Бренд</th>
                <th>Остаток</th>
                <th>Рейтинг</th>
                <th>Действия</th>
            </tr>

            <c:forEach var="p"
                       items="${products}">

                <tr>

                    <td>
                        <c:choose>
                            <c:when test="${not empty p.imageUrl}">
                                <img src="${p.imageUrl}"
                                     alt="${p.name}"
                                     class="admin-thumb"/>
                            </c:when>
                            <c:otherwise>
                                -
                            </c:otherwise>
                        </c:choose>
                    </td>

                    <td>${p.id}</td>

                    <td>
                        <c:out value="${p.name}"/>
                    </td>

                    <td>${p.price} ₽</td>

                    <td>
                        <c:out value="${p.brand}"/>
                    </td>

                    <td>
                        ${p.stock}

                        <c:if test="${p.stock > 0 && p.stock <= 5}">
                            <span class="warning-text">мало</span>
                        </c:if>

                        <c:if test="${p.stock <= 0}">
                            <span class="error-text">нет</span>
                        </c:if>
                    </td>

                    <td>
                        <c:choose>
                            <c:when test="${p.reviewsCount > 0}">
                                <fmt:formatNumber value="${p.averageRating}"
                                                  maxFractionDigits="1"/>
                                ★ (${p.reviewsCount})
                            </c:when>
                            <c:otherwise>
                                -
                            </c:otherwise>
                        </c:choose>
                    </td>

                    <td>

                        <form method="get"
                              action="admin"
                              class="inline-form">

                            <input type="hidden"
                                   name="action"
                                   value="editProduct"/>

                            <input type="hidden"
                                   name="id"
                                   value="${p.id}"/>

                            <button>
                                Редактировать
                            </button>

                        </form>

                        <form method="post"
                              action="admin"
                              class="inline-form">

                            <input type="hidden"
                                   name="action"
                                   value="deleteProduct"/>

                            <input type="hidden"
                                   name="id"
                                   value="${p.id}"/>

                            <button onclick="return confirm('Удалить товар?')"
                                    class="danger-button">
                                Удалить
                            </button>

                        </form>

                    </td>

                </tr>

            </c:forEach>

        </table>

    </div>

    <br/>

    <div class="section-card">

        <h2>
            <c:choose>
                <c:when test="${discount != null && discount.id > 0}">
                    Редактирование скидки
                </c:when>
                <c:otherwise>
                    Создание скидки / промокода
                </c:otherwise>
            </c:choose>
        </h2>

        <form method="post"
              action="admin">

            <input type="hidden"
                   name="action"
                   value="saveDiscount"/>

            <input type="hidden"
                   name="id"
                   value="${discount.id}"/>

            <label>Название</label>
            <br/>

            <input type="text"
                   name="name"
                   value="${discount.name}"
                   placeholder="Например: Летняя акция"
                   required/>

            <br/><br/>

            <label>Тип скидки</label>
            <br/>

            <select name="type">
                <option value="PERCENT"
                    <c:if test="${discount.type == 'PERCENT'}">
                        selected
                    </c:if>>
                    Процентная
                </option>

                <option value="FIXED"
                    <c:if test="${discount.type == 'FIXED'}">
                        selected
                    </c:if>>
                    Фиксированная
                </option>
            </select>

            <br/><br/>

            <label>Значение</label>
            <br/>

            <input type="number"
                   step="0.01"
                   name="value"
                   value="${discount.value}"
                   placeholder="Например: 10"
                   required/>

            <br/><br/>

            <label>Промокод</label>
            <br/>

            <input type="text"
                   name="code"
                   value="${discount.code}"
                   placeholder="Например: SPORT10"/>

            <p class="muted">
                Если поле промокода пустое, скидка будет работать как акция для выбранных товаров.
            </p>

            <label>Дата начала</label>
            <br/>

            <input type="datetime-local"
                   name="startDate"/>

            <br/><br/>

            <label>Дата окончания</label>
            <br/>

            <input type="datetime-local"
                   name="endDate"/>

            <br/><br/>

            <label>
                <input type="checkbox"
                       name="active"
                    <c:if test="${discount == null || discount.active}">
                        checked
                    </c:if>/>
                Активна
            </label>

            <br/><br/>

            <label>ID товаров для акции</label>

            <p class="muted">
                Введите ID товаров через запятую. Для промокода можно оставить пустым.
            </p>

            <textarea name="productIds"
                      rows="3"
                      placeholder="Например: 1, 2, 3">${discountProductIdsText}</textarea>

            <br/><br/>

            <button>
                Сохранить скидку
            </button>

            <c:if test="${discount != null && discount.id > 0}">
                <a href="admin"
                   class="reset-link">
                    Отменить редактирование
                </a>
            </c:if>

        </form>

        <br/>

        <h3>Скидки и промокоды</h3>

        <table class="data-table">

            <tr>
                <th>ID</th>
                <th>Название</th>
                <th>Тип</th>
                <th>Значение</th>
                <th>Промокод</th>
                <th>Активна</th>
                <th>Период</th>
                <th>Действия</th>
            </tr>

            <c:forEach var="d"
                       items="${discounts}">

                <tr>

                    <td>${d.id}</td>
                    <td><c:out value="${d.name}"/></td>
                    <td>${d.type}</td>
                    <td>${d.value}</td>

                    <td>
                        <c:choose>
                            <c:when test="${not empty d.code}">
                                ${d.code}
                            </c:when>
                            <c:otherwise>
                                -
                            </c:otherwise>
                        </c:choose>
                    </td>

                    <td>
                        <c:choose>
                            <c:when test="${d.active}">
                                Да
                            </c:when>
                            <c:otherwise>
                                Нет
                            </c:otherwise>
                        </c:choose>
                    </td>

                    <td>
                        ${d.startDate} — ${d.endDate}
                    </td>

                    <td>

                        <form method="get"
                              action="admin"
                              class="inline-form">

                            <input type="hidden"
                                   name="action"
                                   value="editDiscount"/>

                            <input type="hidden"
                                   name="id"
                                   value="${d.id}"/>

                            <button>
                                Редактировать
                            </button>

                        </form>

                        <form method="post"
                              action="admin"
                              class="inline-form">

                            <input type="hidden"
                                   name="action"
                                   value="deleteDiscount"/>

                            <input type="hidden"
                                   name="id"
                                   value="${d.id}"/>

                            <button class="danger-button"
                                    onclick="return confirm('Удалить скидку?')">
                                Удалить
                            </button>

                        </form>

                    </td>

                </tr>

            </c:forEach>

        </table>

    </div>

    <br/>

    <div class="section-card">

        <h2>Заказы</h2>

        <table class="data-table">

            <tr>
                <th>ID</th>
                <th>User ID</th>
                <th>Сумма</th>
                <th>Скидка</th>
                <th>Промокод</th>
                <th>Доставка</th>
                <th>Адрес</th>
                <th>Статус</th>
                <th>Дата</th>
                <th>Статус</th>
                <th>История статусов</th>
                <th>Возврат</th>
            </tr>

            <c:forEach var="o"
                       items="${orders}">

                <tr>

                    <td>${o.id}</td>
                    <td>${o.userId}</td>
                    <td>${o.totalPrice} ₽</td>
                    <td>${o.discountAmount} ₽</td>

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

                    <td>${o.deliveryPrice} ₽</td>

                    <td>
                        <c:out value="${o.deliveryAddress}"/>
                    </td>

                    <td>${o.status}</td>

                    <td>${o.createdAt}</td>

                    <td>

                        <form method="post"
                              action="admin">

                            <input type="hidden"
                                   name="action"
                                   value="updateStatus"/>

                            <input type="hidden"
                                   name="orderId"
                                   value="${o.id}"/>

                            <select name="status">

                                <c:forEach var="status"
                                           items="${orderStatuses}">

                                    <option value="${status}"
                                        <c:if test="${o.status == status}">
                                            selected
                                        </c:if>>
                                        ${status}
                                    </option>

                                </c:forEach>

                            </select>

                            <button>
                                Сохранить
                            </button>

                        </form>

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

                        <form method="post"
                              action="admin">

                            <input type="hidden"
                                   name="action"
                                   value="refundOrder"/>

                            <input type="hidden"
                                   name="orderId"
                                   value="${o.id}"/>

                            <input type="text"
                                   name="reason"
                                   placeholder="Причина возврата"/>

                            <button class="danger-button"
                                    onclick="return confirm('Выполнить возврат и отменить заказ?')">
                                Возврат
                            </button>

                        </form>

                    </td>

                </tr>

            </c:forEach>

        </table>

    </div>

    <br/>

    <div class="section-card">

        <h2>Пользователи</h2>

        <table class="data-table">

            <tr>
                <th>ID</th>
                <th>Email</th>
                <th>Роль</th>
                <th>Заблокирован</th>
                <th>Дата регистрации</th>
                <th>Действия</th>
            </tr>

            <c:forEach var="u"
                       items="${users}">

                <tr>

                    <td>${u.id}</td>

                    <td>
                        <c:out value="${u.email}"/>
                    </td>

                    <td>${u.role}</td>

                    <td>
                        <c:choose>
                            <c:when test="${u.blocked}">
                                Да
                            </c:when>
                            <c:otherwise>
                                Нет
                            </c:otherwise>
                        </c:choose>
                    </td>

                    <td>${u.createdAt}</td>

                    <td>

                        <form method="post"
                              action="admin">

                            <input type="hidden"
                                   name="action"
                                   value="updateUser"/>

                            <input type="hidden"
                                   name="userId"
                                   value="${u.id}"/>

                            <select name="role">
                                <option value="CLIENT"
                                    <c:if test="${u.role == 'CLIENT'}">
                                        selected
                                    </c:if>>
                                    CLIENT
                                </option>

                                <option value="ADMIN"
                                    <c:if test="${u.role == 'ADMIN'}">
                                        selected
                                    </c:if>>
                                    ADMIN
                                </option>
                            </select>

                            <label>
                                <input type="checkbox"
                                       name="blocked"
                                    <c:if test="${u.blocked}">
                                        checked
                                    </c:if>/>
                                Заблокирован
                            </label>

                            <button>
                                Сохранить
                            </button>

                        </form>

                    </td>

                </tr>

            </c:forEach>

        </table>

    </div>

    <br/>

    <div class="section-card">

        <h2>Модерация отзывов</h2>

        <table class="data-table">

            <tr>
                <th>ID</th>
                <th>User</th>
                <th>Product ID</th>
                <th>Оценка</th>
                <th>Комментарий</th>
                <th>Статус</th>
                <th>Дата</th>
                <th>Действие</th>
            </tr>

            <c:forEach var="r"
                       items="${reviews}">

                <tr>

                    <td>${r.id}</td>
                    <td><c:out value="${r.userEmail}"/></td>
                    <td>${r.productId}</td>
                    <td>${r.rating}</td>
                    <td><c:out value="${r.comment}"/></td>
                    <td>${r.status}</td>
                    <td>${r.createdAt}</td>

                    <td>

                        <form method="post"
                              action="admin">

                            <input type="hidden"
                                   name="action"
                                   value="updateReviewStatus"/>

                            <input type="hidden"
                                   name="reviewId"
                                   value="${r.id}"/>

                            <select name="status">
                                <option value="APPROVED"
                                    <c:if test="${r.status == 'APPROVED'}">
                                        selected
                                    </c:if>>
                                    APPROVED
                                </option>

                                <option value="REJECTED"
                                    <c:if test="${r.status == 'REJECTED'}">
                                        selected
                                    </c:if>>
                                    REJECTED
                                </option>

                                <option value="PENDING"
                                    <c:if test="${r.status == 'PENDING'}">
                                        selected
                                    </c:if>>
                                    PENDING
                                </option>
                            </select>

                            <button>
                                Сохранить
                            </button>

                        </form>

                    </td>

                </tr>

            </c:forEach>

        </table>

    </div>

    <br/>

    <div class="section-card">

        <h2>Отчёты по продажам</h2>

        <h3>Продажи по дням</h3>

        <table class="data-table">

            <tr>
                <th>Дата</th>
                <th>Заказы</th>
                <th>Выручка</th>
            </tr>

            <c:forEach var="row"
                       items="${salesReport}">

                <tr>
                    <td>${row.period}</td>
                    <td>${row.ordersCount}</td>
                    <td>${row.revenue} ₽</td>
                </tr>

            </c:forEach>

        </table>

        <br/>

        <h3>Популярные товары</h3>

        <table class="data-table">

            <tr>
                <th>Product ID</th>
                <th>Название</th>
                <th>Продано</th>
                <th>Выручка</th>
            </tr>

            <c:forEach var="row"
                       items="${popularReport}">

                <tr>
                    <td>${row.productId}</td>
                    <td><c:out value="${row.productName}"/></td>
                    <td>${row.soldQuantity}</td>
                    <td>${row.revenue} ₽</td>
                </tr>

            </c:forEach>

        </table>

        <br/>

        <h3>Эффективность промокодов</h3>

        <table class="data-table">

            <tr>
                <th>Промокод</th>
                <th>Скидка</th>
                <th>Заказы</th>
                <th>Сумма скидок</th>
            </tr>

            <c:forEach var="row"
                       items="${discountReport}">

                <tr>
                    <td>${row.promoCode}</td>
                    <td><c:out value="${row.discountName}"/></td>
                    <td>${row.ordersCount}</td>
                    <td>${row.totalDiscount} ₽</td>
                </tr>

            </c:forEach>

        </table>

    </div>

</div>

</body>
</html>