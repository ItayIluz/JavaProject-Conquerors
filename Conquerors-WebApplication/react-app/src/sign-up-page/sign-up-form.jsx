import React, { Component } from "react";
import { Redirect } from "react-router-dom";
import { getDataFrom, postActionTo} from '../server-functions.js';
import "./sign-up-form.css";

const SERVLET_PATH = "signUp";

class SignUpForm extends Component {
  
  constructor() {
    super();

    this.state = {
      inputResult: "",
      messageTimeoutID: -1,
      connectedSuccessfully: false,
      inGameTitle: null,
      playerUsername: null
    }

    this.playerUsernameInput = React.createRef();
    this.handleSignUp = this.handleSignUp.bind(this);
    this.updateInputResultMessage = this.updateInputResultMessage.bind(this);
  }
  
  componentDidMount(){
    // Check if user is already logged
    getDataFrom(SERVLET_PATH, responseJSON => {
      if(responseJSON.result == "CONNECTED")
        this.setState({connectedSuccessfully: true, playerUsername: responseJSON.playerUsername, inGameTitle: responseJSON.inGameTitle});
    });
  }

  // Validate and send data to server and show a message based on the results
  handleSignUp(event) {
    event.preventDefault();

    let userInput = this.playerUsernameInput.current.value.trim();

    if(userInput == ""){
      this.updateInputResultMessage("Please enter your player name!");
    } else {
      postActionTo(SERVLET_PATH, "postUsername", {username: userInput}, responseJSON => {
        if(responseJSON.result == "SUCCESS"){
          this.setState({connectedSuccessfully: true, playerUsername: userInput});
        } else {
          this.updateInputResultMessage(responseJSON.result);
        }
      });
    }
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

    if(this.state.connectedSuccessfully){
      if(this.state.inGameTitle != null)
        return <Redirect to={{
          pathname: "/Conquerors/conquerors-game", 
          state: { playerUsername: this.state.playerUsername, gameTitle: this.state.inGameTitle }
        }}/>;
      else
        return <Redirect to={{
          pathname: "/Conquerors/games-room" , 
          state: { playerUsername: this.state.playerUsername }
        }}/>;
    }

    return (
      <div>
        <form className="sign-up-form">
          <div className="form-field-container">
            <label htmlFor="form-player-username-input">Your Player Name:</label>
              <input id="form-player-username-input" ref={this.playerUsernameInput} name="playerUserName" type="text" className="form-field-input" />
          </div>
            <button className="my-button" onClick={this.handleSignUp}>Login</button>
          <div className={"input-result" + (this.state.inputResult === "" ? " hidden" : "")}>
            {this.state.inputResult}
          </div>
        </form>
      </div>
    );
  }
}
export default SignUpForm;