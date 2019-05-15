import React, { Component } from "react";
import "./sign-up-form.css";

class SignUpForm extends Component {
  constructor() {
    super();

    this.state = {
      playerName: "",
      inputResult: "",
      messageTimeoutID: -1,
    }

    this.handleSignUp = this.handleSignUp.bind(this);
    this.checkForFormErrors = this.checkForFormErrors.bind(this);
    this.updateInputResultMessage = this.updateInputResultMessage.bind(this);
  }
  
  checkForFormErrors(){
    if(!this.state.playerName)
      return "Please enter your name."

    return "";
  }

  // Validate and send data to server and show a message based on the results
  async handleSignUp(event) {
    event.preventDefault();
    let finalResult = this.checkForFormErrors();

    // If there are no validation errors, post data to the server
    if(finalResult === ""){
      const response = await fetch('/api/send', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
          userID: this.props.userID,
          playerName: this.state.playerName,
          amountToSend: this.state.amountToSend
        }),
      });
      
      const reponseResult = await response.json(); 

      if (response.status !== 200) throw Error(reponseResult.message);

      if(reponseResult.result === "SUCCESS"){
        finalResult = "Transaction commited successfully!";
        this.props.afterSubmit(this.state.amountToSend);
      } else {
        finalResult = "Transaction failed!";
      }
      this.setState({playerName: "", amountToSend: ""})
    }

    this.updateInputResultMessage(finalResult)
  }

  // Update the submit result and hide it after 3 seconds
  updateInputResultMessage(message){
    this.setState({inputResult: message});
    if(this.state.messageTimeoutID !== -1)
      clearTimeout(this.state.messageTimeoutID);

    let messageTimeout = setTimeout(() => this.setState({inputResult: "", messageTimeoutID: -1}), 3000);
    this.setState({messageTimeoutID: messageTimeout});
  }

  render() {
    return (
      <div>
        <form className="sign-up-form">
          <div className="form-field-container">
            <label htmlFor="form-receiver-input">Player Name:</label>
              <input id="form-receiver-input" name="playerName" type="text" className="form-field-input" />
          </div>
            <button className="my-button" onClick={this.handleSignUp}>Play</button>
          <div className={"input-result" + (this.state.inputResult === "" ? " hidden" : "")}>
            {this.state.inputResult}
          </div>
        </form>
      </div>
    );
  }
}
export default SignUpForm;