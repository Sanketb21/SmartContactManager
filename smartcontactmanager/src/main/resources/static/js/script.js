console.log("This is my script file");

function toggleSidebar() {
    
    const sidebar =  document.getElementsByClassName("sidebar")[0];
    const content =  document.getElementsByClassName("content")[0];

    if(window.getComputedStyle(sidebar).display === "none"){
        sidebar.style.display = "block";
        content.style.marginLeft = "20%";
    }
    else{
        sidebar.style.display = "none";
        content.style.marginLeft = "0%";
    }
};

// function search(){
//     let query = document.getElementById("search-input").value;
    
//     if(query==""){
//         document.querySelector(".search-result").style.display = "none";
//     }
//     else{
//         //search
//         console.log(query);

//         //sending request to server
//         let url = `http://localhost:8282/search/${query}`;

//         fetch(url).then((response=>{
//             return response.json();
//         })
//         .then((data)=>{
//             //data
//             console.log(data);
//         })
//         ) ;  

//         document.querySelector(".search-result").style.display = "block";
//     }
// }

function search(){
    let query = document.getElementById("search-input").value;
    
    if(query==""){
        document.querySelector(".search-result").style.display = "none";
    }
    else{
        //search
        console.log(query);

        //sending request to server
        let url = `http://localhost:8282/search/${query}`;

        fetch(url)
        .then(response => {
            return response.json();
        })
        .then(data => {
            //data
            console.log(data);

            let text=`<div class='list-group'>`

            data.forEach((contact)=>{
                text+=`<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'>${contact.name}</a>`
            });

            text+=`</div>`;

            document.querySelector(".search-result").innerHTML = text;
            document.querySelector(".search-result").style.display = "block";
        });
        
       
    }
}

