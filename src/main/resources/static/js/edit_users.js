document.addEventListener("DOMContentLoaded", () => {
    const editForm = document.querySelector("#editForm");
    const editId = document.querySelector("#editId");
    const editFirstName = document.querySelector("#editFirstName");
    const editLastName = document.querySelector("#editLastName");
    const editEmail = document.querySelector("#editEmail");
    const editPassword = document.querySelector("#editPassword");
    const editRoles = document.querySelector("#editRoles");

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    if (editForm) {
        editForm.addEventListener("submit", e => {
            e.preventDefault();
            const updatedUser = {
                id: Number(editId.value),
                firstName: editFirstName.value,
                lastName: editLastName.value,
                email: editEmail.value,
                roles: Array.from(editRoles.selectedOptions).map(opt => ({id: Number(opt.value), name: opt.text}))
            };

            if (editPassword.value && editPassword.value.trim() !== "") {
                updatedUser.password = editPassword.value;
            }

            console.log("Payload dikirim ke server:", updatedUser);

            fetch(`/admin/api/${editId.value}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(updatedUser)
            })
                .then(res => {
                    console.log("Response status:", res.status); // LOG di sini cukup
                    if (!res.ok) throw new Error("Update failed");
                    return res.json();
                })
                .then(() => {
                    alert("User updated successfully");
                    editPassword.value = ""; // reset field password
                    window.location.href = "/admin"; // kembali ke list
                })
                .catch(err => alert(err.message));
        });
    }
}