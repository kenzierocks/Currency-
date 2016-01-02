/*
 * This file is part of Currency☃, licensed under the MIT License (MIT).
 *
 * Copyright (c) kenzierocks (Kenzie Togami) <http://kenzierocks.me>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.kenzierocks.plugins.currencysnowmen.implementation;

import java.util.UUID;

import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Identifiable;

public class CSUniqueAccount extends CSAccount implements UniqueAccount {

    private static Text mapIdentToText(Identifiable ident) {
        Text base = Text.of(ident.getUniqueId().toString());
        if (ident instanceof Player) {
            Player user = (Player) ident;
            // TODO when DisplayNameData is implemented swap
            base = user.get(DisplayNameData.class)
                    .flatMap(dnd -> dnd.displayName().getDirect()).orElse(base);
        } else if (ident instanceof User) {
            User user = (User) ident;
            base = Text.of(user.getName());
        } else if (ident instanceof Subject) {
            Subject user = (Subject) ident;
            base = Text.of(user.getIdentifier());
        }
        return base;
    }

    private final UUID uuid;

    public CSUniqueAccount(Identifiable ident) {
        super(ident.getUniqueId().toString(), mapIdentToText(ident));
        this.uuid = ident.getUniqueId();
    }

    public CSUniqueAccount(UUID uuid) {
        super(uuid.toString());
        this.uuid = uuid;
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

}
