$(document).ready(function(){
	
	$('#loginSubmit').on('click', function(event) {
		event.preventDefault();
		var emailInput = $('#emailInput');
		var passwordInput = $('#passwordInput');
		
		var email = emailInput.val();
		var password = passwordInput.val();
		
		if($('#emailInput').val() == "" || $('#passwordInput').val() == ""){
            alert('Niste popunili sva polja!');
            return;
        }
		if($('#emailInput').val() == ""){
            alert('Niste popunili polje email!');
            return;
        }
		if($('#passwordInput').val() == ""){
            alert('Niste popunili polje lozinka!');
            return;
        }
		
		$.post('api/users/user/login', {'email': email, 'password': password},
			function(response){
				var userEmail = response.email;
				sessionStorage.setItem('userEmail', userEmail);
				if(response.authority.name == 'Admin'){
					window.location.href = 'adminStranica.html';
				}else {
					window.location.href = 'index.html';
				}
		}).fail(function(){
			console.log("error")
		});
	});
});