document.addEventListener("DOMContentLoaded", () => {
    const tableBody = document.querySelector("#userTableBody");
    const currentRolesMeta = document.querySelector('meta[name="current_user_roles"]');
    const currentRoles = currentRolesMeta ? currentRolesMeta.content.split(",").map(r => r.trim()) : [];
    const currentUserMeta = document.querySelector('meta[name="current_user_id"]');
    currentUserMeta ? parseInt(currentUserMeta.content) : null;
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    // Modals & forms
    const editModal = document.querySelector("#editUserModal");
    const editForm = document.querySelector("#editForm");
    const editId = document.querySelector("#editId");
    const editFirstName = document.querySelector("#editFirstName");
    const editLastName = document.querySelector("#editLastName");
    const editEmail = document.querySelector("#editEmail");
    const editPassword = document.querySelector("#editPassword");
    const editRoles = document.querySelector("#editRoles");

    const addForm = document.querySelector("#addForm");
    const addModal = document.querySelector("#addUserModal");
    const addFirstName = document.querySelector("#addFirstName");
    const addLastName = document.querySelector("#addLastName");
    const addEmail = document.querySelector("#addEmail");
    const addPassword = document.querySelector("#addPassword");
    const addRoles = document.querySelector("#addRoles");


    // =======================
    // Fetch & render users
    // =======================
    function fetchUsers() {
        const url = window.location.pathname.startsWith("/user") ? "/user/api/me" : "/admin/api/all";

        fetch(url)
            .then(res => res.json())
            .then(data => {
                tableBody.innerHTML = "";
                const users = Array.isArray(data) ? data : [data];

                users.forEach(user => {
                    const { id, firstName, lastName, email, roles = [] } = user;
                    const roleNames = roles.map(r => r.name).join(", ");

                    let actionTd = "";
                    if (currentRoles.includes("ROLE_ADMIN")) {
                        actionTd = `
                                <td>
                                    <button class="btn btn-sm btn-primary edit" data-id="${id}">Edit</button>
                                    <button class="btn btn-sm btn-danger delete" data-id="${id}">Delete</button>
                                </td>
                            `;

                    }


                    const tr = document.createElement("tr");
                    tr.innerHTML = `
                        <td>${id}</td>
                        <td>${firstName}</td>
                        <td>${lastName}</td>
                        <td>${email}</td>
                        <td>${roleNames}</td>
                        ${actionTd}
                    `;
                    tableBody.appendChild(tr);
                });
            })
            .catch(err => console.error("Fetch users failed:", err));
    }

    fetchUsers();

    // =======================
    // Delete user
    // =======================
    tableBody.addEventListener("click", e => {
        if (e.target.classList.contains("delete")) {
            const id = e.target.dataset.id;
            if (confirm("Do you really want to delete this user?")) {
                fetch(`/admin/api/${id}`, {
                    method: "DELETE",
                    headers: { [csrfHeader]: csrfToken }
                }).then(() => fetchUsers());
            }
        }
    });

    // =======================
    // Edit user
    // =======================
    tableBody.addEventListener("click", e => {
        if (e.target.classList.contains("edit")) {
            const id = e.target.dataset.id;
            fetch(`/admin/api/${id}`)
                .then(res => res.json())
                .then(user => {
                    editId.value = user.id;
                    editFirstName.value = user.firstName;
                    editLastName.value = user.lastName;
                    editEmail.value = user.email;
                    editPassword.value = "";

                    Array.from(editRoles.options).forEach(opt => {
                        opt.selected = user.roles.some(r => r.id === Number(opt.value));
                    });

                    new bootstrap.Modal(editModal).show();
                });
        }
    });

    if (editForm) {
        editForm.addEventListener("submit", e => {
            e.preventDefault();

            const updatedUser = {
                id: editId.value,
                firstName: editFirstName.value,
                lastName: editLastName.value,
                email: editEmail.value,
                password: editPassword.value,
                roles: Array.from(editRoles.selectedOptions).map(opt => ({ id: Number(opt.value), name: opt.text }))
            };

            fetch(`/admin/api/${editId.value}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(updatedUser)
            })
                .then(res => {
                    if (!res.ok) throw new Error("Update failed");
                    return res.json();
                })
                .then(() => {
                    bootstrap.Modal.getInstance(editModal).hide();
                    fetchUsers();
                })
                .catch(err => alert(err.message));
        });
    }

    // =======================
    // Create user
    // =======================
    if (addForm) {
        addForm.addEventListener("submit", e => {
            e.preventDefault();

            const newUser = {
                firstName: addFirstName.value,
                lastName: addLastName.value,
                email: addEmail.value,
                password: addPassword.value,
                roles: Array.from(addRoles.selectedOptions).map(opt => ({ id: Number(opt.value), name: opt.text }))
            };

            fetch("/admin/api", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(newUser)
            })
                .then(res => {
                    if (!res.ok) throw new Error("Create failed");
                    return res.json();
                })
                .then(() => {
                    bootstrap.Modal.getInstance(addModal).hide();
                    addForm.reset();
                    fetchUsers();
                })
                .catch(err => alert(err.message));
        });
    }
});
