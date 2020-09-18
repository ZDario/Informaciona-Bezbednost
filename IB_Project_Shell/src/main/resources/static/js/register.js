$(document).ready(function(){
	
	$('#registrationSubmit').on('click', function(event) {
		event.preventDefault();
		var emailInput = $('#emailInput');
		var passwordInput = $('#passwordInput');
		var passwordInputRepeat = $('#repeatedPasswordInput');
		
		var email = emailInput.val();
		var password = passwordInput.val();
		var passwordRepeat = passwordInputRepeat.val();
		
		if($('#emailInput').val() == "" || $('#passwordInput').val() == "" || $('#repeatedPasswordInput').val() == ""){
            alert('Neka od obaveznih polja su prazna!');
            return;
        }
		if($('#emailInput').val() == ""){
            alert('Email polje je prazno!');
            return;
        }
		if($('#passwordInput').val() == ""){
            alert('Polje lozinka je prazno!');
            return;
        }
		if($('#repeatedPasswordInput').val() == ""){
            alert('Polje za ponovljenu lozinku je prazno!');
            return;
        }
		if($('#passwordInput').val() != $('#repeatedPasswordInput').val()){
            alert('Uneli ste razlicite lozinke!');
            return;
        }
		
		$.post('api/users/user/registration', {'email': email, 'password': password},
			function(response){
				alert('Uspesna registracija!');
	            window.location.replace("korisnikStranica.html");
		}).fail(function(){
			console.log("error")
		});
	});
});