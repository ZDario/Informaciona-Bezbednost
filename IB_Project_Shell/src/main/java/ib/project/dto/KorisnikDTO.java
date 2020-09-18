package ib.project.dto;

public class KorisnikDTO {

		private Long id;
		private String email;
		private String password;
		private String username;
		private String path;
		
		public KorisnikDTO(String email, String password, String username) {
			this.email = email;
			this.password = password;
			this.username = username;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}
		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		@Override
		public String toString() {
			return "KorisnikDTO [id=" + id + ", email=" + email 
					+ ", password=" + password + ", username=" + username + ", path=" + path + "]";
		}
		
		


	}
