const search_btn = document.getElementById("search_btn");
const placa = document.getElementById("placa");

window.addEventListener('load', function(){
    placa.innerText = " - "
})

search_btn.addEventListener("click", function(){
    search();
})

async function search(){
  try{
        const response = await fetch("/connect");
        if(!response.ok){
            placa.innerText = "Não encontrada!"
        }
        else{
            const result = await response.text();
            placa.innerText = result;
        }

    }
    catch(err){
        console.log(err)
    }
}