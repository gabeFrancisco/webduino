const search_btn = document.getElementById("search_btn");
const standby_btn = document.getElementById("standby");
const ativar_btn = document.getElementById("ativar")
let connected = false;

const placa = document.getElementById("placa");
const estado = document.getElementById("estado");

window.addEventListener('load', function () {
    placa.innerText = " - "
    estado.innerText = " - "
    enableCommandPannelInputs();
})

function enableCommandPannelInputs() {
    const commandPannel = document.getElementById("command_panel");
    const elements = commandPannel.querySelectorAll("button, input");

    elements.forEach(el => {
        if (!connected) {
            el.disabled = true;
            commandPannel.style.backgroundColor = "#999"
            commandPannel.style.color = "#555"
        }
        else {
            el.disabled = false;
            commandPannel.style.backgroundColor = "#ddd"
            commandPannel.style.color = "#000"
        }
    })

}

search_btn.addEventListener("click", function () {
    search();
})

standby_btn.addEventListener("click", function () {
    setStandBy();
})

ativar_btn.addEventListener("click", function () {
    setActivate();
})

async function setActivate() {
    const response = await getFetchResult("/activate");
    if (!response.ok) {
        estado.innerHTML = "Erro! Verifique o servidor!"
        estado.style.color = "red";
    }
    else {
        estado.innerText = "Ativada!";
        estado.style.color = "#1a6e2c"
    }
}

async function setStandBy() {
    const response = await getFetchResult("/standby", "POST");
    if (!response.ok) {
        estado.innerHTML = "Erro! Verifique o servidor!"
        estado.style.color = "red";
    }
    else {
        estado.innerText = "StandBy";
        estado.style.color = "#85871f"
    }

}

async function setAtivar() {

}

async function search() {
    const response = await getFetchResult("/connect", "POST")
    if (!response.ok) {
        placa.innerText = "Não encontrada!"
        connected = false;
    }
    else {
        const result = await response.text();
        placa.innerText = result;
        connected = true;
        enableCommandPannelInputs()
    }

}

async function getFetchResult(route, method) {
    try {
        const response = await fetch(route, { method: method });
        return response;
    }
    catch (err) {
        console.log("Ocorreu um erro na requisição: " + err)
    }
}