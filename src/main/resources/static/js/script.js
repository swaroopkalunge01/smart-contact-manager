console.log("this is java script file")

const toggleSidebar = () => {
	
	if ($('.sidebar').is(":visible"))
	{
		$(".sidebar").css("display","none");
		$(".content").css("margin-left","0%");
	}
	else
	{
		$(".sidebar").css("display","block");
		$(".content").css("margin-left","20%");
	}
	
};


// =======================
// DARK MODE
// =======================


// ENABLE DARK MODE

function enableDarkMode(){

    document.body.classList.add("dark-mode");

    localStorage.setItem("theme","dark");
}


// ENABLE LIGHT MODE

function enableLightMode(){

    document.body.classList.remove("dark-mode");

    localStorage.setItem("theme","light");
}


// LOAD THEME ON PAGE REFRESH

window.onload = function(){

    let theme = localStorage.getItem("theme");

    if(theme === "dark"){

        document.body.classList.add("dark-mode");
    }
}
function deleteAccount(){

    swal({

        title: "Are you sure?",

        text: "Your account and contacts will be deleted permanently!",

        icon: "warning",

        buttons: true,

        dangerMode: true,

    })

    .then((willDelete) => {

        if(willDelete){

            window.location="/user/delete-account";
        }
    });
}
const search = () => {

    let query =
        document.getElementById("search-input").value;

    if(query === ""){

        document.querySelector(".search-result")
                .style.display = "none";

    }else{

        fetch(`/user/search/${query}`)

        .then((response) => response.json())

        .then((data) => {

            let text = `<div class='list-group'>`;

            data.forEach((contact) => {

                text +=
                `<a href='/user/${contact.cId}/contact'
                    class='list-group-item list-group-item-action'>

                    ${contact.name}

                 </a>`;
            });

            text += `</div>`;

            document.querySelector(".search-result")
                    .innerHTML = text;

            document.querySelector(".search-result")
                    .style.display = "block";
        });
    }
};