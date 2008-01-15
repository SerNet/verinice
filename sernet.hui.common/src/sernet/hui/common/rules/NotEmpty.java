package sernet.hui.common.rules;


public class NotEmpty implements IValidationRule {

	public boolean validate(String userInput, String[] args) {
		return userInput.length() > 0;
	}

}
