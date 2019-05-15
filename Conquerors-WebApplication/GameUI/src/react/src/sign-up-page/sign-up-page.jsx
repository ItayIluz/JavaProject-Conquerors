import React, { Component } from 'react';
import SignUpForm from './sign-up-form.jsx';

class SignUpPage extends Component {

  constructor(){
    super();
  }

  render() {
    return (
      <div className="main">
        <div className="main-header">
          Welcome to the Conquerors Online Game!
        </div>
        <div className="container-header" style={{  width: "25%", margin: "20px auto"}}>
          <div className="container-header-title">Please sign up in order to play</div>
          <div className="container">
            <SignUpForm />
          </div>
        </div>
       
      </div>
    );
  }
}

export default SignUpPage; 
