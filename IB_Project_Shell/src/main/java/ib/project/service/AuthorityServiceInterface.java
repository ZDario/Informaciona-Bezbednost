package ib.project.service;

import ib.project.model.Authority;

public interface AuthorityServiceInterface {
	Authority findById (int id);
	Authority findByName (String name);
}
