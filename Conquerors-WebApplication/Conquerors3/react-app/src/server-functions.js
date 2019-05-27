export function getDataFrom(getPath, responseCallback = undefined, dataToGet = {}){

  let queryParams = "";
  let first = true;

  for(let key in dataToGet){
    queryParams += first ? "?" : "&";

    queryParams += key + "=" + dataToGet[key];

    if(first)
      first = false;
  }

  fetch("/Conquerors/" + getPath + queryParams).then(async response => {
    if(response && responseCallback !== undefined){
      const responseJSON = await response.json();
      responseCallback(responseJSON);
    }
  });
}

export function postActionTo(postPath, actionData, valuesData, responseCallback = undefined){
  fetch('/Conquerors/' + postPath, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ 
      action: actionData,
      values: valuesData
    }),
  }).then(async response => {
    if(response && responseCallback !== undefined){
      const responseJSON = await response.json();
      responseCallback(responseJSON);
    }
  });
}

export default {getDataFrom, postActionTo}