/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autoconfigurerjava.chain;

import autoconfigurerjava.OSType;

/**
 *
 * @author Miquéias Fernandes
 */
public abstract class AbstractSOHandler {

    private AbstractSOHandler next;

    public void setNext(AbstractSOHandler next) {
        if (this.next == null) {
            this.next = next;
        } else {
            this.next.setNext(next);
        }
    }

    public boolean handler(OSType os) {
        if (accept(os)) {
            return handle(os);
        } else if (next != null) {
            return next.handler(os);
        }
        return false;
    }

    public abstract boolean accept(OSType os);

    public abstract boolean handle(OSType os);
}
