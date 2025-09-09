document.addEventListener("DOMContentLoaded", () => {
    const tableBody = document.querySelector("#userTableBody");
    const currentRolesMeta = document.querySelector('meta[name="current_user_roles"]');
    const currentRoles = currentRolesMeta ? currentRolesMeta.content.split(",").map(r => r.trim()) : [];
    const currentUserMeta = document.querySelector('meta[name="current_user_id"]');
    const currentUserId = currentUserMeta ? parseInt(currentUserMeta.content) : null;

    console.log("rolesMeta raw:", currentRolesMeta);
    console.log("currentRoles parsed:", currentRoles);


    function fetchUsers() {
        const url = window.location.pathname.startsWith("/user") ? "/user/api/me" : "/admin/api/all";

        fetch(url)
            .then(res => res.json())
            .then(data => {
                tableBody.innerHTML = "";
                const users = Array.isArray(data) ? data : [data];

                users.forEach(user => {
                    // Destructure supaya editor ngerti variabelnya
                    const { id, firstName, lastName, email, roles = [] } = user;
                    const roleNames = Array.from(roles).map(r => r.name).join(", ");

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
    // Event delete
    // =======================
    tableBody.addEventListener("click", e => {
        if (e.target.classList.contains("delete")) {
            const id = e.target.dataset.id;
            if (confirm("Do you really want to delete this user?")) {
                const token = document.querySelector('meta[name="_csrf"]').content;
                const header = document.querySelector('meta[name="_csrf_header"]').content;

                fetch(`/admin/api/${id}`, {
                    method: "DELETE",
                    headers: { [header]: token }
                }).then(() => fetchUsers());
            }
        }
    });

    // =======================
    // Event edit
    // =======================
    tableBody.addEventListener("click", e => {
        if (e.target.classList.contains("edit")) {
            const id = e.target.dataset.id;
            window.location.href = `/admin/edit/${id}`;
        }
    });
});
