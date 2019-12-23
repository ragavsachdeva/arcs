/**
 * @license
 * Copyright 2019 Google LLC.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * Code distributed by Google as part of this project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */

'use strict';

/* global defineParticle */


defineParticle(({SimpleParticle, html}) => {

  const template = html`Hello, <span>{{num1}}</span>!
  <div slotid="inputSlot1"></div>`;

  return class extends SimpleParticle {
    get template() {
      return template;
    }

    // We need the person handle within shouldRender, so it has to be passed in.
    shouldRender({person1}) {
      // Here we check that the person is defined.
      return person1;
    }

    // Just like with shouldRender, we need access to person, so declare it needs to be passed in.
    render({person1}) {
      // We want the num from person to be interpolated into the template.
      console.log(person1);
      return {
        num1: person1 && person1.num || 0
      };
    }
  };
});
