function nextQ(current, next) {
    let answer = document.getElementById(current).value.trim();

    if (answer === "") {
        alert("Please answer before continuing");
        return;
    }

    document.getElementById(next).style.display = "block";
}

function goToNextPage(page) {
    window.location.href = page;
}

function goToSpin() {
    window.location.href = "spin.html";
}